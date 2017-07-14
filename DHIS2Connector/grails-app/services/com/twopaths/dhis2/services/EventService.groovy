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

package com.twopaths.dhis2.services

import com.twopaths.dhis2.api.ApiStrategy
import com.twopaths.dhis2.api.ApiVersion
import com.twopaths.dhis2.api.Result
import groovyx.net.http.ContentType

/**
 * Service to do Event CRUD with the DHIS 2 API
 */
class EventService {

    final def PATH = "/events"

    def apiService

    /**
     * Creates an Event via the DHIS 2 API
     * @param auth DHIS 2 Credentials
     * @param event The Event object to create
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return The Result of the API creation
     */
    def create(def auth, def event, def query = [:], ApiVersion apiVersion = null) {

        def result = post(auth, event, query, apiVersion)

        log.debug "create, result: ${result}"

        return result
    }

    /**
     * Makes a POST to the events API
     *
     * @param auth DHIS 2 credentials
     * @param body The body of the post
     * @param query Map of query paramaters to use
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def post (def auth, def body, def query = [:], ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, body, query, ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Updates an Event via the DHIS 2 API
     * Note that the API response for this is not standard, so until the DHIS 2 API response is fixed, need special
     * logic to modify the Result object for this.
     *
     * @param auth DHIS 2 Credentials
     * @param event The event object to update
     * @param eventId Id of the event to update
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return The manually modified Result object
     */
    def update(def auth, def event, def eventId, def query = [:],
               ApiVersion apiVersion = null) {

        def result = apiService.put(auth, PATH, event, eventId, query, ContentType.JSON, apiVersion)

        def message = result?.message

        // This is a workaround to cater for the fact that a 'response' is not returned so we just check for the content of the message field and construct fake data
        if (message?.startsWith("Import was successful.")) {
            result.importCount?.updated = 1
            result.succeeded = 1
            result.success = true

        } else {

            result.importCount?.ignored = 1
        }

        return result
    }

    /**
     * Deletes the specified event
     *
     * @param auth DHIS 2 credentials
     * @param eventId The id of the event to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def eventId, ApiVersion apiVersion = null) {

        def path = "${PATH}/${eventId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Bulk deletes all the events specified
     *
     * @param auth DHIS 2 credentials
     * @param eventsToDelete List of event objects to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def bulkDelete (def auth, ArrayList<Map<String,String>> eventsToDelete, ApiVersion apiVersion = null) {
        // to bulk delete, the body is POSTed with strategy=DELETE

        def query = [strategy: ApiStrategy.DELETE.value()]

        def body = [events: eventsToDelete]

        def result = post(auth, body, query, apiVersion)

        return result

    }

    /**
     * Finds Events based on the query parameters supplied
     *
     * @param auth DHIS 2 Credentials
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return found events
     */
    def findByQuery(def auth, def query, ApiVersion apiVersion = null) {

        def events = apiService.get(auth, PATH, query, null, apiVersion)?.data

        return events
    }

    /**
     * Finds events with the specified program stage and tracked entity instance
     *
     * @param auth DHIS 2 Credentials
     * @param programStageId Id of the program stage to find events for
     * @param trackedEntityInstanceId Id of the tracked entity instance to find events for
     * @param apiVersion ApiVersion to use
     * @return found events
     */
    def findByProgramAndProgramStageId(def auth, def programId, def programStageId, def query = [:],
                                       ApiVersion apiVersion = null) {

        query <<  [program: programId, programStage: programStageId]

        return findByQuery(auth, query, apiVersion)
    }

    /**
     * Finds all events for the specified program stage and tracked entity instance
     *
     * @param auth DHIS 2 credentials
     * @param programStageId The id of the program stage to find events for
     * @param trackedEntityInstanceId The id of the tracked entity instance to find events for
     * @param apiVersion DHIS 2 api version
     * @return found Events if any
     */
    def findByProgramStageIdAndTrackedEntityInstanceId(def auth, def programStageId, def trackedEntityInstanceId,
                                                       ApiVersion apiVersion = null) {

        def queryParams = [programStage: programStageId, trackedEntityInstance: trackedEntityInstanceId]

        return findByQuery(auth, queryParams, apiVersion)
    }

    /**
     * Retrieves the event with the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the event for
     * @param fields array of fields to return in the event
     * @param apiVersion ApiVersion to use
     * @return the event found
     */
    def get(def auth, def code, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (code) {
            queryParams.put("filter", "code:eq:${code}")
        }

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return findByQuery(auth, queryParams, apiVersion)

    }
}
