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

import com.twopaths.dhis2.api.ApiVersion
import grails.transaction.Transactional
import groovyx.net.http.ContentType

/**
 * Service to do OptionValue (Option) CRUD with the DHIS 2 API
 */
@Transactional
class OptionValueService {

    final def PATH = "/options"

    def apiService

    /**
     * Creates an optionValue (option) via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param optionValue The optionValue (option) to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def optionValue, ApiVersion apiVersion = null) {

        log.debug ">>> optionValue: " + optionValue

        // remove the id
        optionValue.remove('id')

        def result = apiService.post(auth, PATH, optionValue, [:], ContentType.JSON, apiVersion)

        log.debug "<<< optionValue, result: " + result

        return result
    }

    /**
     * Updates an optionValue (option)
     *
     * @param auth DHIS 2 Credentials
     * @param optionValue The optionValue (option) to update
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def update(def auth, def optionValue, ApiVersion apiVersion = null) {

        log.debug ">>> optionValue: " + optionValue

        def result = apiService.put(auth, PATH, optionValue, optionValue.id, [:], ContentType.JSON, apiVersion)

        log.debug "<<< result: " + result

        return result
    }

    /**
     * Deletes the specified option value (option)
     *
     * @param auth DHIS 2 credentials
     * @param optionValueId Id of the option value (option) to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def optionValueId, ApiVersion apiVersion = null) {

        def path = "${PATH}/${optionValueId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Retrieves the optionValue (option) with the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the optionValue (option) for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The optionValue (option) found if any
     */
    def get(def auth, def code, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def optionValue = apiService.get(auth, "${PATH}?code=${code}", queryParams, null,
                apiVersion)?.data

        log.debug "optionValue: " + optionValue

        return optionValue
    }
}
