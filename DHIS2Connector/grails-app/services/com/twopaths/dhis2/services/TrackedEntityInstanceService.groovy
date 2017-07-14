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
import grails.transaction.Transactional
import groovyx.net.http.ContentType

/**
 * Service to do TrackedEntityInstance CRUD with the DHIS 2 API
 */
@Transactional
class TrackedEntityInstanceService {

    final def PATH = "/trackedEntityInstances"

    def apiService

    /**
     * Creates a trackedEntityInstance
     *
     * @param auth DHIS 2 Credentials
     * @param trackedEntityInstance the trackedEntityInstance to create
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def trackedEntityInstance, def query=[:],
               ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, trackedEntityInstance, query, ContentType.JSON, apiVersion)

        log.debug "create, result: " + result

        return result
    }

    /**
     * Updates a trackedEntityInstance
     *
     * @param auth DHIS 2 Credentials
     * @param trackedEntityInstance The trackedEntityInstance to update
     * @param trackedEntityInstanceId The id of the trackedEntityInstance to update
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the update
     */
    def update(def auth, def trackedEntityInstance, def trackedEntityInstanceId, def query=null,
               ApiVersion apiVersion = null) {

        def result = apiService.put(auth, PATH, trackedEntityInstance, trackedEntityInstanceId, query,
                ContentType.JSON, apiVersion)

        log.debug "update, result: " + result

        return result
    }

    /**
     * Deletes the specified tracked entity instance
     *
     * @param auth DHIS 2 credentials
     * @param trackedEntityInstanceId Id of the tracked entity instance to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def trackedEntityInstanceId, ApiVersion apiVersion = null) {

        def path = "${PATH}/${trackedEntityInstanceId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Bulk deletes all the specified tracked entity instances
     *
     * @param auth DHIS 2 credentials
     * @param trackedEntityInstancesToDelete The List of tracked entity instances to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def bulkDelete (def auth, ArrayList<Map<String,String>> trackedEntityInstancesToDelete, ApiVersion apiVersion = null) {

        def query = [strategy: ApiStrategy.DELETE.value()]

        def body = [trackedEntityInstances: trackedEntityInstancesToDelete]

        def result = apiService.post(auth, PATH, body, query, ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Retrieves a trackedEntityInstance by the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code of the trackedEntityInstance to get
     * @param fields array of fields to return from the API
     * @param apiVersion ApiVersion to use
     * @return The trackedEntityInstance found if any
     */
    def get(def auth, def code, ArrayList<String> fields = [], ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (code) {
            queryParams.put("filter", "code:eq:${code}")
        }

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        log.debug "get, code: " + code

        def trackedEntityInstance = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        log.debug "trackedEntityInstance: " + trackedEntityInstance

        return trackedEntityInstance
    }

    /**
     * Retrieves all trackedEntityInstances matching the supplied query parameters
     *
     * @param auth DHIS 2 Credentials
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return List of trackedEntityInstances found
     */
    def findByQuery(def auth, def query=[:], ApiVersion apiVersion = null) {

            def trackedEntities = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data

            return trackedEntities
    }

    /**
     * Retrieves the id of a trackedEntityInstance by organisationUnit, attribute, and value
     *
     * @param auth DHIS 2 Credentials
     * @param orgUnit The organisationUnit to find the trackedEntityInstance for
     * @param attribute The attribute to find the trackedEntityInstance for
     * @param value The falue to find the trackedEntityInstance for
     * @param apiVersion ApiVersion to use
     * @return The id of the found trackedEntityInstance if any
     */
    def getIdByOrgUnitAttributeAndValue(def auth, def orgUnit, def attribute, def value,
                                        ApiVersion apiVersion = null) {

        log.debug "getIdByOrgUnitAttributeAndValue, orgUnit: ${orgUnit}, attribute: ${attribute}, value: ${value}"

        def id

        def queryParams = [ou:orgUnit, filter: "${attribute}:eq:${value}"]

        def trackedEntityInstances = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        if (trackedEntityInstances?.trackedEntityInstances) {
            if (trackedEntityInstances?.trackedEntityInstances?.size() == 1) {
                id =  trackedEntityInstances.trackedEntityInstances[0].trackedEntityInstance
            } else if (trackedEntityInstances?.trackedEntityInstances?.size() == 0) {
                log.error "No rows for orgUnit: " + orgUnit + " and attribute: " + attribute
            } else {
                log.error "Multiple rows for orgUnit: " + orgUnit + " and attribute: " + attribute
            }
        }

        log.debug "trackedEntityInstance id: " + id

        // Return the id
        return id
    }

    /**
     * Creates a lookup map of TrackedEntityInstances by the TrackedEntityInstance's code
     *
     * @param auth DHIS 2 Credentials
     * @param fields array of fields to return from the API
     * @param apiVersion ApiVersion to use
     * @return a lookup map of TrackedEntityInstances by the TrackedEntityInstance's code
     */
    def getLookup(def auth, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allTrackedEntityInstances = []

        def trackedEntityInstances = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        allTrackedEntityInstances.addAll(trackedEntityInstances.trackedEntityInstances)

        // Create the lookup from the tracked entity attributes
        allTrackedEntityInstances.each { trackedEntityInstance ->
            lookup << [("${trackedEntityInstance.code}".toString()): trackedEntityInstance]
        }

        return lookup
    }
}
