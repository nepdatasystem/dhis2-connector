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
 * Service to do Program CRUD with the DHIS 2 API
 */
@Transactional
class ProgramService {

    final def PATH = "/programs"
    final def PATH_PROGRAM_TRACKED_ENTITY_ATTRIBUTES = "programTrackedEntityAttributes"
    final def DELETIONS = "deletions"

    def apiService

    /**
     * Creates a Program via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param program The program to create
     * @param apiVersion ApiVersion to use
     * @return The Result of the API creation
     */
    def create(def auth, def program, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, program, [:], ContentType.JSON, apiVersion)

        log.debug "program: " + result

        return result
    }

    /**
     * Updates a Program via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param program The program to update
     * @param mergeMode The mergeMode to use for the update
     * @param apiStrategy The apiStrategy to use for the update
     * @param apiVersion ApiVersion to use
     * @return The Result of the API update
     */
    def update(def auth, def program, ApiMergeMode mergeMode = ApiMergeMode.MERGE,
               ApiStrategy apiStrategy = ApiStrategy.CREATE_AND_UPDATE,
               ApiVersion apiVersion = null) {

        def query = [mergeMode: mergeMode.value(), strategy: apiStrategy.value()]

        // Default mergeMode in 2.24 and prior is "MERGE". Default in 2.25 and higher is "REPLACE"
        def result = apiService.put(auth, PATH, program, program.id, query, ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Deletes the specified program
     *
     * @param auth DHIS 2 credentials
     * @param programId The id of the program to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete(def auth, def programId, ApiVersion apiVersion = null) {
        log.debug ">>> program: " + programId

        def path = "${PATH}/${programId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< program delete, result: " + result

        return result

    }

    /**
     * Finds all Programs with the specified parameters
     *
     * @param auth DHIS 2 Credentials
     * @param queryParams Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return All programs found matching the supplied criteria
     */
    def findAll(def auth, def queryParams = [fields : ":all"],
                ApiVersion apiVersion = null) {

        def programs = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        return programs
    }

    /**
     * Retrieves a program with the specified id
     *
     * @param auth DHIS 2 Credentials
     * @param programId The id of the program to retrieve
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return The found program if any
     */
    def get(def auth, def programId, def query=[:], ApiVersion apiVersion = null) {

        def program = apiService.get(auth, "${PATH}/${programId}", query, null, apiVersion)?.data

        log.debug "program: " + program

        return program
    }
}
