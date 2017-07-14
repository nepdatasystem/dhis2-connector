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

import com.twopaths.dhis2.api.ApiMergeMode
import com.twopaths.dhis2.api.ApiVersion
import grails.transaction.Transactional
import groovyx.net.http.ContentType

/**
 * Service to do TrackedEntityAttribute CRUD with the DHIS 2 API
 */
@Transactional
class TrackedEntityAttributeService {

    final def PATH = "/trackedEntityAttributes"

    def apiService

    /**
     * Creates a trackedEntityAttribute
     *
     * @param auth DHIS 2 Credentials
     * @param trackedEntityAttribute The trackedEntityAttribute to create
     * @param apiVersion ApiVersion to use
     * @return the Result of the API creation
     */
    def create(def auth, def trackedEntityAttribute, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, trackedEntityAttribute, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Updates a trackedEntityAttribute
     *
     * @param auth DHIS 2 Credentials
     * @param trackedEntityAttribute The trackedEntityAttribute to update
     * @param mergeMode The mergeMode to use for the update
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return the Result of the API update
     */
    def update(def auth, def trackedEntityAttribute, def mergeMode = ApiMergeMode.REPLACE, def query = [:],
               ApiVersion apiVersion = null) {

        query.put("mergeMode", mergeMode.value())

        def result = apiService.put(auth, PATH, trackedEntityAttribute, trackedEntityAttribute.id, query,
                ContentType.JSON, apiVersion)

        log.debug "update, result: " + result

        return result
    }

    /**
     * Deletes the specified tracked entity attribute
     *
     * @param auth DHIS 2 credentials
     * @param trackedEntityAttributeId The id of the tracked entity attribute to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def trackedEntityAttributeId, ApiVersion apiVersion = null) {

        def path = "${PATH}/${trackedEntityAttributeId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Finds a TrackedEntityAttribute by the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the TrackedEntityAttribute for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The found trackedEntityAttribute if any
     */
    def findByCode(def auth, def code, ArrayList<String> fields = [":all"],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "code:eq:${code}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def data = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        def trackedEntityAttribute

        if (data?.trackedEntityAttributes) {
            trackedEntityAttribute = data?.trackedEntityAttributes[0]
        }

        log.debug "trackedEntityAttribute: " + trackedEntityAttribute

        return trackedEntityAttribute
    }

    /**
     * Finds the id of the trackedEntityAttribute for the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the trackedEntityAttribute id for
     * @param apiVersion ApiVersion to use
     * @return The id of the trackedEntityAttribute found if any
     */
    def getIdFromCode(def auth, def code, ApiVersion apiVersion = null) {

        log.debug "getIdFromCode, code: " + code

        def trackedEntityAttribute = findByCode(auth, code, ["id"], apiVersion)

        if (trackedEntityAttribute) {
            return trackedEntityAttribute.id
        } else {
            return null
        }
    }

    /**
     * Creates a lookup map of trackedEntityAttributes by code
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return a lookup map of trackedEntityAttributes by code
     */
    def getLookup(def auth, ArrayList<String> fields = [":all"], ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allTrackedEntityAttributes = []

        def trackedEntityAttributes = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        if (trackedEntityAttributes) {
            allTrackedEntityAttributes.addAll(trackedEntityAttributes.trackedEntityAttributes)

            // Create the lookup from the tracked entity attributes
            allTrackedEntityAttributes.each { trackedEntityAttribute ->
                lookup << [("${trackedEntityAttribute.code}".toString()): trackedEntityAttribute]
            }
        }

        return lookup
    }
}
