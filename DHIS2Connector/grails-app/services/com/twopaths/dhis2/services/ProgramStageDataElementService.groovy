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
 * Service to do ProgramStageDataElement CRUD with the DHIS 2 API
 */
@Transactional
class ProgramStageDataElementService {

    final def PATH = "/programStageDataElements"

    def apiService

    /**
     * Creates a programStageDataElement
     *
     * @param auth DHIS 2 Credentials
     * @param programStageDataElement
     * @param apiVersion ApiVersion to use
     * @return The Result of the API creation
     */
    def create(def auth, def programStageDataElement, ApiVersion apiVersion = null) {

        log.debug "programStageDataElement.create"

        def result = apiService.post(auth, PATH, programStageDataElement, [:], ContentType.JSON, apiVersion)

        log.debug "programStage: " + result

        return result
    }

    /**
     * Updates a programStageDataElement
     *
     * @param auth DHIS 2 Credentials
     * @param programStageDataElement
     * @param apiStrategy
     * @param apiVersion ApiVersion to use
     * @return the Result of the API update
     */
    def update(def auth, def programStageDataElement, ApiStrategy apiStrategy = ApiStrategy.CREATE_AND_UPDATE,
               ApiVersion apiVersion = null) {

        def query = [strategy: apiStrategy.value()]

        log.debug "programStageDataElement.update"

        def result = apiService.put(auth, PATH, programStageDataElement, programStageDataElement.id, query,
                ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Retrieves all programStageDataElements
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The full list of programStageDataElements
     */
    def findAll(def auth, ArrayList<String> fields = [":all"],
                ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        log.debug "programStageDataElements.findAll"

        def programStageDataElements = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        return programStageDataElements
    }
}
