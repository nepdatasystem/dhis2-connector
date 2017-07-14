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

import com.twopaths.dhis2.api.ApiActionType
import com.twopaths.dhis2.api.AbstractApiResultParser
import com.twopaths.dhis2.api.Result
import grails.transaction.Transactional
import org.apache.http.HttpStatus

/**
 * Parser of Results using the 2.23 version of the API.
 * Note that in version 2.23 the only implementation is for the /api/23/metadata call
 *
 * When upgrading to this version from the default version, please note the following changes in the response object
 * and behaviour:
 *
 *  - If the response has a global status of "ERROR", there is no description of the error
 *  - If there is an error in one of the metadata objects, the other metadata objects will not execute
 *  - "lastImported" is no longer returned
 *  - There is no status returned for each "typeReport" (formerly "importTypeSummary")
 *  - "conflicts" are no longer returned from DHIS 2, but are replaced with "errorReports" which are in a
 *    different structure. This parser will parse those "errorReports" back into the traditional "conflicts" structure
 *    of "object" and "value".
 *
 *    Example errorReport from DHIS 2:
 *    [
 *    "message": "Maximum length of property \"shortName\"is 50, but given length was 150.",
 *    "mainKlass": "org.hisp.dhis.program.Program",
 *    "errorKlass": "java.lang.String",
 *    "errorCode": "E4001"
 *    ]
 *
 *    Example of our Result conflict structure:
 *    [
 *    "object": "AUG18TEST2",
 *    "value": "Validation Violations: [ErrorReport{message=Maximum length of property \"shortName\"is 50, but given length was 150., errorCode=E4001, mainKlass=class org.hisp.dhis.program.Program, errorKlass=class java.lang.String, value=null}]"
 *    ]
 *
 *  - The "errorReports" only return request body indices for the error but no actual object values,
 *    now necessitating the passing in of the requestBody to parse for helpful text.
 *    This is clunky because here is an example of a request body and and response:
 *
 *    --- Request body: ---
 *    "programs":[
 *    ....
 *    ]
 *
 *    --- Response: ---
 *
 *    "objectReports": [
 *    [
 *        "klass": "org.hisp.dhis.program.Program",
 *        "index": 0,
 *        "errorReports": [
 *        ...
 *        ]
 *    ]
 *    We have to match the klass in the response with the camelCase pluralized key required in the request
 *
 */
@Transactional
class ApiResultParser223Service extends AbstractApiResultParser {

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

        // Some API calls return text instead of JSON...which is a bug...and can probably be removed at some stage...
        if (status == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            result.success = false
            result.errors << [code: "dhis2.error", args: [action.value(), "DHIS 2 Internal Server Error"]]
        } else {

            result.importCount = data?.stats
            // the stat "imported" has changed to "created"
            // keep "created" so that either can be used
            result.importCount?.imported = result.importCount?.created

            // no description for error is supplied in response.
            // Decision to skip this whole error because specific errors will be spit out from the inner loop
            // but keeping placeholder in case this is augmented later by DHIS 2
            /*
            if (data.status == "ERROR" || data.status == "WARNING") {
                result.errors << [code: "dhis2.error", args: [action, ""]]
            }
            */

            // 2.23 versioned API for Metadata has this returned
            if (data?.typeReports) {
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

                            def objectInfo = objectWithError?.id ?: objectWithError?.name ?: "Unknown"
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
                            importCount : importCount,
                            succeeded : succeeded,
                            success : success,
                            conflicts : conflicts
                    ]

                    if (typeReport.conflicts) {
                        result.conflicts?.addAll(typeReport.conflicts)
                    }

                    result.importTypeSummaries.put(
                            getObjectNameFromFullyQualifiedObjectName(report.klass), typeReport)
                }
            }
        }

        // HTTP status
        result.status = status


        // Iterate through the import conflicts and get the errors
        result.conflicts?.each { conflict ->
            result.errors << [code: "dhis2.error", args: [conflict."object", conflict?.value]]
        }

        // import/update/delete count of success/failures
        if (action == ApiActionType.Import || action == ApiActionType.Update || action == ApiActionType.Delete) {
            result.succeeded = getSucceededCount(action, result?.importCount)
        }

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

        // Set message too
        result.message = data?.message


        // If doing a get, then set the data
        if (action == ApiActionType.Get) {
            if (data) {
                result.data = data
                result.success = true
            } else {
                result.success = false
            }
        }
        return result
    }
}
