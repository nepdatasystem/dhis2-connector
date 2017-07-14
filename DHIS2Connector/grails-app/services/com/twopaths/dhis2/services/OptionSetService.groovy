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
import com.twopaths.dhis2.api.ApiStrategy
import com.twopaths.dhis2.api.ApiVersion
import grails.transaction.Transactional
import groovyx.net.http.ContentType


/**
 * Service to do OptionSet CRUD with the DHIS 2 API
 */
@Transactional
class OptionSetService {

    final def PATH = "/optionSets"
    final def PATH_OPTIONS = "/options"

    def apiService

    /**
     * Creates an option set via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param optionSet The optionSet to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def optionSet, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, optionSet, [:], ContentType.JSON, apiVersion)

        log.debug "create, result: " + result

        return result
    }

    /**
     * Adds an option to an option set
     *
     * @param auth DHIS 2 Credentials
     * @param optionSetId The Id of the option set to have the option added to
     * @param optionValueId The id of the option to add to the option set
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def addOption(def auth, def optionSetId, def optionValueId,
                  ApiVersion apiVersion = null) {

        log.debug ">>> addOption, optionSetId: " + optionSetId + ", optionValueId: " + optionValueId

        def result = apiService.post(auth, "$PATH/${optionSetId}$PATH_OPTIONS/${optionValueId}", null, [:],
                ContentType.JSON, apiVersion )

        log.debug "<<< addOption: " + result

        return result
    }

    /**
     * Updates an option set
     *
     * @param auth DHIS 2 Credentials
     * @param optionSet The option set to update
     * @param apiStrategy The ApiStrategy to use for the update
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def update(def auth, def optionSet, ApiStrategy apiStrategy = ApiStrategy.CREATE_AND_UPDATE,
               ApiVersion apiVersion = null) {

        def query = [strategy: apiStrategy.value()]

        def result = apiService.put(auth, PATH, optionSet, optionSet.id, query, ContentType.JSON, apiVersion)

        log.debug "update: " + result

        return result
    }

    /**
     * Retrieves an option set by id
     *
     * @param auth DHIS 2 Credentials
     * @param id The id of the option set to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return the Option Set found if any
     */
    def getById(def auth, def id, ArrayList<String> fields = [],
                ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def optionSet = apiService.get(auth, "$PATH/${id}", queryParams, null, apiVersion)?.data

        log.debug "getById, id: " + id + ", optionSet: " + optionSet

        return optionSet
    }

    /**
     * Gets a single option set with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name Name of the option set to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The option set found if any
     */
    def get(def auth, def name, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def optionSets = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.optionSets

        log.debug "optionSets: " + optionSets

        def optionSet
        if (optionSets.size() == 1) {
            optionSet = optionSets[0]
        }

        log.debug "optionSet: " + optionSet

        return optionSet
    }

    /**
     * Gets a single option set with the specified name
     * Same as "get" method but included for backwards compatibility
     *
     * @param auth DHIS 2 Credentials
     * @param name Name of the option set to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The option set found if any
     */
    def findByName(def auth, def name, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        return get(auth, name, fields, apiVersion)
    }

    /**
     * Creates a lookup map of Option Sets by the option set's code
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return a lookup map of option sets by code
     */
    def getLookup(def auth, ArrayList<String> fields = [":all", "options[id,code,name,displayName]"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allOptionSets = []

        def optionSets = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.optionSets

        if (optionSets) {
            allOptionSets.addAll(optionSets)

            // Create the lookup from the option sets
            allOptionSets.each { optionSet ->
                lookup << [("${optionSet.id}".toString()): optionSet]
            }
        }
        return lookup
    }
}
