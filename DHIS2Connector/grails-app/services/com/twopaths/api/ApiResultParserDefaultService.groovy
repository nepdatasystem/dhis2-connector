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
 * Parser of Results using the default version of the API
 * Note that newer versions of the API have errors that only return indices of the objects
 * from the request body, necessitating the passing in of the requestBody to the parse method.
 * This version of the API does not need to use the request body.
 */
@Transactional
class ApiResultParserDefaultService extends AbstractApiResultParser {

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
        }
        else if (data instanceof StringReader) {
            result.data = data?.text
        } else {
            if (data?.response) { // JSON response
                def importSummary
                // Only interested in conflicts if import, update or delete
                if (action != ApiActionType.Get) {
                    // New response data
                    if (data?.response?.size()) {
                        log.debug "data.response.conflicts: ${data.response.conflicts}"
                        importSummary = data.response

                        // If responseType == importSummaries, get the importSummary
                        if (data.response?.responseType == "ImportSummaries") {
                            // we don't currently have any queries that would return more than one summary here.
                            // The only one that returns more than one summary is the /metadata/ call, which
                            // is not wrapped in a response, and is handled with the parsing of importTypeSummaries
                            importSummary = data.response?.importSummaries?.get(0)
                        }
                        if (importSummary?.conflicts != null) {
                            result.conflicts = importSummary.conflicts
                        } else if (importSummary?.importConflicts != null) {
                            result.conflicts = importSummary.importConflicts
                        }
                        result.importCount = importSummary.importCount
                    }
                }

                // Check importSummary for error status
                if (importSummary?.status == "ERROR") {
                    result.errors << [code: "dhis2.error", args: [action.value(), importSummary.description]]
                }

                // Check importSummary for warning status
                if (importSummary?.status == "WARNING") {
                    result.errors << [code: "dhis2.error", args: [action.value(), importSummary.description]]
                }

                // lastImported
                if (action == ApiActionType.Import || action == ApiActionType.Update) {
                    // for data values there is no response key but also no last imported
                    result.lastImported = importSummary?.lastImported
                    result.reference = importSummary?.reference
                }
            } else { // Non-response data format. Data Value import is like this. Also 2.23 Metadata API is like this
                log.debug "data.conflicts: ${data?.conflicts}"
                if (data?.conflicts != null) {
                    result.conflicts = data.conflicts
                } else if (data?.importConflicts != null) {
                    result.conflicts = data.importConflicts
                }
                result.importCount = data?.importCount

                // 2.22+ default (unversioned) API for Metadata has this returned
                if (data?.importTypeSummaries) {
                    data.importTypeSummaries.each { summary ->

                        def importTypeSummary = [
                                importCount : summary?.importCount,
                                lastImported : summary?.lastImported,
                                succeeded : getSucceededCount(action, summary?.importCount),
                                conflicts : summary?.importConflicts ?: [],
                                status : summary.status

                        ]
                        importTypeSummary.put("success",  importTypeSummary.succeeded != 0
                                && importTypeSummary.conflicts?.size() == 0
                                && importTypeSummary.status == "SUCCESS"
                                && importTypeSummary.importCount?.ignored == 0)

                        if (summary?.status == "ERROR") {
                            result.errors << [code: "dhis2.error", args: [action.value(), summary.type + ": " + summary.description]]
                        }

                        // Check importSummary for warning status
                        if (summary?.status == "WARNING") {
                            result.errors << [code: "dhis2.error", args: [action.value(), summary.type + ": " + summary.description]]
                        }

                        if (importTypeSummary.conflicts) {
                            result.conflicts?.addAll(importTypeSummary.conflicts)
                        }

                        result.importTypeSummaries.put(
                                summary.type, importTypeSummary)
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
        // Note that there now seems to be edge cases where there is not success but there is no error
        // Consumer apps will need to handle this case.
        // If we were to add an unspecified error, we have no concept of internationalization in the connector
        // so it would have to be in English
        return result
    }


}
