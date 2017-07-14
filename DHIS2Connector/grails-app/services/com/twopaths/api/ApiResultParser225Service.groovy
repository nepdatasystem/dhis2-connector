/*
 * Copyright (c) 2014-2017. Institute for International Programs at Johns Hopkins University.
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the NEP project, Institute for International Programs, 
 * Johns Hopkins University nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.twopaths.api

import com.twopaths.dhis2.api.AbstractApiResultParser
import com.twopaths.dhis2.api.ApiActionType
import com.twopaths.dhis2.api.ApiResponseType
import com.twopaths.dhis2.api.Result
import grails.transaction.Transactional
import org.apache.http.HttpStatus

/**
 * Parser of Results using the 2.25 version of the API.
 *
 * When upgrading to this version from version 2.24, there appear to have been no changes in the response object
 * and behaviour.
 *
 */
@Transactional
class ApiResultParser225Service extends AbstractApiResultParser {

    /**
     * Parses the DHIS 2 API response into a consistent Result object for consumption
     *
     * @param action The ApiActionType
     * @param data data returned by the API
     * @param status The response status
     * @param requestBody The requestBody submitted to the API if any
     * @return the parsed Result object
     */
    @Override
    Result parse(ApiActionType action, def data, def status, def requestBody = null) {

        Result result = new Result()

        // Set message
        result.message = data?.message

        // Some API calls return text instead of JSON...which is a bug...and can probably be removed at some stage...
        if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            result.success = false
            result.errors << [code: "dhis2.error", args: [action.value(), result.message?:"DHIS 2 Internal Server Error"]]
        }

        // If doing a get, then set the data
        if (action == ApiActionType.Get) {
            if (data) {
                result.data = data
                result.success = true
            } else {
                result.success = false
            }
        }
        // This will be for action = Import, Update or Delete
        else {
            // if this response is an ObjectReport, we need to extract the single object response info and transform it
            // into the standard Result structure
            // This is new in 2.24
            if (data?.response?.responseType == ApiResponseType.ObjectReport.value()) {
                // this will mutate/populate the Result object
                parseObjectReport(action, status, data, result)

            } else if (action == ApiActionType.Delete && data?.status == ERROR) {
                // posting a delete can return a general error
                // if the status is 500, we've already added the general error above
                if (status != HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                    result.errors << [code: "dhis2.error", args: [action.value(), data?.message]]
                }
                result.importCount = [
                        "deleted" : 0,
                        "ignored" : 1,
                        "imported": 0,
                        "created" : 0,
                        "updated" : 0
                ]
                // note tracked entity instance bulk delete return status 201 SC_CREATED which is inconsistent
                // with event bulk delete which returns status 200 SC_OK
            } else if (status in  [
                    HttpStatus.SC_OK,
                    HttpStatus.SC_CREATED,
                    HttpStatus.SC_CONFLICT]) {
                // this will mutate/populate the Result object
                parseImportSummary(data, action, requestBody, result)

            }



            // HTTP status
            result.status = status

            // Iterate through the import conflicts and get the errors
            result.conflicts?.each { conflict ->
                result.errors << [code: "dhis2.error", args: [conflict."object", conflict?.value]]
            }

            // if there is a conflict and no specific error but there is a message, add it to the errors
            if (status == HttpStatus.SC_CONFLICT && !result.errors && result.message) {
                result.errors << [code: "dhis2.error", args: [action.value(), result.message]]
            }

            // import/update/delete count of success/failures
            result.succeeded = getSucceededCount(action, result?.importCount)

            // 204 = no body but OK
            if (status == HttpStatus.SC_NO_CONTENT) {
                result.success = true
            } else {
                result.success = (
                        result.succeeded != 0
                                && result.errors.size() == 0
                                && result.conflicts.size() == 0
                                && result.importCount?.ignored == 0
                )
            }
        }

        return result
    }

    /**
     * Method to parse out ObjectReport (new in v-2.24)
     * Mutates the Result object
     *
     * @param actionType The ApiActionType
     * @param status The response status
     * @param data data returned by the API
     * @param result The Result object that will be mutated
     */
    private void parseObjectReport(ApiActionType actionType, def status, def data, Result result) {
        // 2.24 returns status code = 201 (Created) for created objects instead of 200 (OK)
        if (status == HttpStatus.SC_CREATED) {
            //make assumption there is a uid when the http status is CREATED
            result.lastImported = data?.response?.uid
            // assume that there is only one imported because object reports are for only one object
            result.importCount = [
                    "deleted" : 0,
                    "ignored" : 0,
                    "imported": 1,
                    "created" : 1,
                    "updated" : 0
            ]
        }
        // 2.24 returns status code = 200 (OK) for updated or deleted objects
        else if (status == HttpStatus.SC_OK) {
            result.lastImported = data?.response?.uid
            // assume that there is only one imported because object reports are for only one object
            result.importCount = [
                    "deleted" : actionType == ApiActionType.Delete ? 1 : 0,
                    "ignored" : 0,
                    "imported": 0,
                    "created" : 0,
                    "updated" : (actionType == ApiActionType.Update || actionType == ApiActionType.Import) ? 1 : 0
            ]
        }
        // 2.24 returns status code = 409 (Conflict) for created objects if there are errors instead of 200 (OK) with inner conflicts
        else if (status == HttpStatus.SC_CONFLICT) {
            result.importCount = [
                    "deleted" : 0,
                    "ignored" : 1,
                    "imported": 0,
                    "created" : 0,
                    "updated" : 0
            ]
            result.conflicts = []
            if (data?.response?.errorReports) {
                data.response.errorReports.each { errorReport ->
                    String objectName = getObjectNameFromFullyQualifiedObjectName(errorReport.mainKlass)
                    result.conflicts.add([
                            "object": objectName,
                            "value" : errorReport.toString()
                    ])
                }
            }
        }
    }

    /**
     * Method to parse out info when Http Status is OK (200)
     * This method mutates the Result object
     *
     * @param data The data returned by the API call
     * @param action The ApiActionType
     * @param requestBody The request body from the original request. The import summary errors are returned by requestBody indices
     * @param result The Result object that will be mutated
     */
    private void parseImportSummary(def data, ApiActionType action, def requestBody, Result result) {
        def importSummary
        // 2.23/2.24 versioned API for Metadata has this returned
        if (data?.typeReports) {
            // This method will mutate the Result object
            importSummary = parseTypeReports(data, action, requestBody, result)
        } else if (data?.response?.size()) { // JSON response

            log.debug "data.response.conflicts: ${data.response.conflicts}"
            importSummary = data.response

        } else if (data?.responseType in ["ImportSummary", "ImportSummaries"]) {
            importSummary = data
        }

        // Note that ImportSummaries returned are inconsistend: can either be at the top level of the data object,
        // or could be wrapped in data.response. This is taken care of by the above logic.
        if (importSummary?.responseType == "ImportSummaries") {
            // If responseType == importSummaries, get the flattened importSummary with errors (if any) extracted
            importSummary = parseImportSummaries(importSummary)
        }

        if (importSummary?.conflicts != null) {
            result.conflicts = importSummary.conflicts
        } else if (importSummary?.importConflicts != null) {
            result.conflicts = importSummary.importConflicts
        }
        result.importCount = importSummary?.importCount

        // Check importSummary for error status
        // Type report errors will be embedded and already catered to
        if (importSummary?.status == ERROR && !data?.typeReports) {
            result.errors << [code: "dhis2.error", args: [action.value(), importSummary.description]]
        }

        // Check importSummary for warning status
        if (importSummary?.status == WARNING) {
            result.errors << [code: "dhis2.error", args: [action.value(), importSummary.description]]
        }

        // lastImported
        if (action == ApiActionType.Import || action == ApiActionType.Update) {
            // for data values there is no response key but also no last imported
            if (importSummary?.reference) {
                result.lastImported = importSummary.reference
                result.reference = importSummary.reference
            }
        }
        // the stat "imported" has changed to "created"
        // keep "created" so that either can be used
        if (result.importCount?.imported == null) {
            result.importCount?.imported = result.importCount?.created
        }
        if (result.importCount?.created == null) {
            result.importCount?.created = result.importCount?.imported
        }

        // no description for error is supplied in response.
        // Decision to skip this whole error because specific errors will be spit out from the inner loop
        // but keeping placeholder in case this is augmented later by DHIS 2
        /*
        if (data.status == ERROR || data.status == WARNING) {
            result.errors << [code: "dhis2.error", args: [action, ""]]
        }
        */
    }

    /**
     * This method will parse out the ImportSummaries. Some calls that return ImportSummaries are for instances
     * the bulk deletion of Events and Tracked Entity Instances
     *
     * @param importSummary The import summary to parse
     * @return The extracted importSummary
     */
    private LinkedHashMap<String, Object> parseImportSummaries(importSummary) {
        def extractedSummary
        if (importSummary?.importSummaries?.size() == 1) {
            extractedSummary = importSummary?.importSummaries?.get(0)
        } else if (importSummary?.importSummaries?.size() > 1) {
            // now posting multiple objects for deletion (eg: events), so need to get the overall counts,
            // which are already in the top level of the data.response, then extract any errors

            extractedSummary = [
                    responseType: "ImportSummary",
                    status      : importSummary.status,
                    importCount : [
                            imported: importSummary.imported,
                            updated : importSummary.updated,
                            deleted : importSummary.deleted,
                            ignored : importSummary.ignored
                    ]
            ]
            // if there were any objects ignored, need to extract specific errors in the importSummaries
            if (importSummary?.ignored > 0) {
                def conflicts = []
                def errorSummaries = importSummary.importSummaries.findAll { it.status == ERROR }
                errorSummaries?.each { errorSummary ->
                    conflicts.add([
                            "object": UNKNOWN,
                            "value" : errorSummary.description
                    ])
                }

                extractedSummary.conflicts = conflicts
            }

        }
        return extractedSummary
    }

    /**
     * This method will parse out the Type Reports. This is currently used by the Metadata API call
     * This method mutates the Result object
     *
     * @param data The data returned by the API call
     * @param action The ApiActionType
     * @param requestBody The request body from the original request. The import summary errors are returned by requestBody indices
     * @param result The Result object that will be mutated
     * @return The import summary containing the parsed type reports
     */
    private def parseTypeReports(def data, ApiActionType action, def requestBody, def result) {
        // the importSummary will just be the data root
        def importSummary = data
        importSummary.importCount = data?.stats

        data.typeReports.each { report ->
            def importCount = report?.stats ?: []
            // the stat "imported" has changed to "created"
            importCount?.imported = importCount?.created

            def succeeded = getSucceededCount(action, importCount)

            def conflicts = []

            // find any errors
            def objectReportsWithErrorReports = report?.objectReports?.findAll {
                it.errorReports
            } ?: []

            // errors are only returned with indices, so need to look up object from original request body
            // We have to match the klass in the response with the camelCase pluralized key required in the
            // request

            objectReportsWithErrorReports.each { objectReport ->
                String objectName = getObjectNameFromFullyQualifiedObjectName(objectReport.klass)
                def pluralizedObjectTypeName = getPluralizedObjectNameFromObjectName(objectName)

                def requestBodyObjectTypeArray = requestBody.get(pluralizedObjectTypeName)

                def objectWithError
                if (objectReport.index >= 0) {

                    objectWithError = requestBodyObjectTypeArray[objectReport.index]
                }

                objectReport.errorReports?.each { errorReport ->

                    def objectInfo = objectWithError?.id ?: objectWithError?.name ?: UNKNOWN
                    // if we have both the id and the name, show both
                    if (objectWithError?.id && objectWithError?.name) {
                        objectInfo += ": $objectWithError.name"
                    }

                    conflicts.add([
                            "object": objectName + ": " + objectInfo,
                            "value" : errorReport.toString()
                    ])
                }
            }

            def success = (succeeded != 0
                    && conflicts?.size() == 0
                    && importCount?.ignored == 0)

            // Note that the type report no longer has a status associated with it
            def typeReport = [
                    importCount: importCount,
                    succeeded  : succeeded,
                    success    : success,
                    conflicts  : conflicts
            ]

            if (typeReport.conflicts) {
                result.conflicts?.addAll(typeReport.conflicts)
            }

            result.importTypeSummaries.put(
                    getObjectNameFromFullyQualifiedObjectName(report.klass), typeReport)
        }
        return importSummary
    }
}
