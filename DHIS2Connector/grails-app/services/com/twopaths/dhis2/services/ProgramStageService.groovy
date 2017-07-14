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
 * Service to do ProgramStage CRUD with the DHIS 2 API
 */
@Transactional
class ProgramStageService {

    final def PATH = "/programStages"
    final def PATH_PROGRAM_STAGE_DATA_ELEMENTS = "/programStageDataElements"

    def apiService

    /**
     * Retrieves a program stage by id
     *
     * @param auth DHIS 2 Credentials
     * @param programStageId The id of the program stage to retrieve
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return The program stage found if any
     */
    def get(def auth, def programStageId, def query=[:],
            ApiVersion apiVersion = null) {

        log.debug "programStage.get, programStageId: ${programStageId}"

        def programStage = apiService.get(auth, "${PATH}/${programStageId}", query, null, apiVersion)?.data

        log.debug "programStage: ${programStage}"

        return programStage
    }

    /**
     * Creates a program stage
     *
     * @param auth DHIS 2 Credentials
     * @param programStage The program stage to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def programStage, ApiVersion apiVersion = null) {

        log.debug "programStage.create"

        def result = apiService.post(auth, PATH, programStage, [:], ContentType.JSON, apiVersion)

        log.debug "programStage: " + result

        return result
    }

    /**
     * Updates a program stage
     *
     * @param auth DHIS 2 Credentials
     * @param programStage The program stage to update
     * @param apiStrategy the apiStrategy to use for the update
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the update
     */
    def update(def auth, def programStage, ApiStrategy apiStrategy = ApiStrategy.CREATE_AND_UPDATE,
               ApiVersion apiVersion = null) {

        def query = [strategy: apiStrategy.value()]

        log.debug "programStage.update"

        def result = apiService.put(auth, PATH, programStage, programStage.id, query, ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Deletes the specified program stage
     *
     * @param auth DHIS 2 credentials
     * @param programStageId The id of the program stage to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete(def auth, def programStageId, ApiVersion apiVersion = null) {
        log.debug ">>> program stage: " + programStageId

        def path = "${PATH}/${programStageId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< program stage delete, result: " + result

        return result

    }

    /**
     * Assigns a program stage data element to the program stage
     *
     * @param auth DHIS 2 Credentials
     * @param programStageId The id of the program stage to add the program stage data element to
     * @param programStageDataElementId The id of the program stage data element to add to the program stage
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignProgramStageDataElement(def auth, def programStageId, def programStageDataElementId,
                                      ApiVersion apiVersion = null) {

        def result = apiService.post(auth, "$PATH/${programStageId}$PATH_PROGRAM_STAGE_DATA_ELEMENTS/${programStageDataElementId}",
                null, [:], ContentType.JSON, apiVersion )

        return result
    }

    /**
     * Retrieves all programStages
     *
     * @param auth DHIS 2 Credentials
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return The full list of programStages
     */
    def findAll(def auth, def query=[fields: ":all"], ApiVersion apiVersion = null) {

        log.debug "programStage.findAll"

        def programStages = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data

        return programStages
    }

    /**
     * Finds all program stage data elements for the specified program stage
     *
     * @param auth DHIS 2 Credentials
     * @param programStageId The id of the program stage to find program stage data element for
     * @param queryParams Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return All found program stage data elements
     */
    def findAllProgramStageDataElements(def auth, def programStageId, def queryParams = [:],
                                        ApiVersion apiVersion = null) {

        def programStageDataElements = apiService.get(auth, "$PATH/${programStageId}$PATH_PROGRAM_STAGE_DATA_ELEMENTS",
                queryParams, null, apiVersion)?.data

        return programStageDataElements
    }
}
