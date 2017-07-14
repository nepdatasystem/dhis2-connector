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
 * Service to do CategoryOption CRUD with the DHIS 2 API
 */
@Transactional
class CategoryOptionService {

    final def PATH = "/categoryOptions"

    def apiService

    /**
     * Retrieves the specified CategoryOption from the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the CategoryOption to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryOption found if any
     */
    def get(def auth, def id, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryOption = apiService.get(auth, "${PATH}/${id}", queryParams, null,
                apiVersion)?.data

        log.debug "categoryOption: " + categoryOption

        return categoryOption
    }

    /**
     * Finds all CategoryOptions with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name The name to retrieve CategoryOptions for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryOption found if any
     */
    def findByName(def auth, def name, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryOptions = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.categoryOptions

        log.debug "categoryOptions: " + categoryOptions

        def categoryOption
        if (categoryOptions.size() == 1) {
            categoryOption = categoryOptions[0]
        }

        log.debug "categoryOption: " + categoryOption

        return categoryOption
    }

    /**
     * Finds all CategoryOptions in the system
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The full list of CategoryOptions
     */
    def findAll(def auth, ApiVersion apiVersion = null) {

        def categoryOptions = apiService.get(auth, "${PATH}", [:], null, apiVersion)?.data

        log.debug "categoryOptions: " + categoryOptions

        return categoryOptions
    }

    /**
     * Creates a categoryOption
     *
     * @param auth DHIS 2 Credentials
     * @param categoryOption The categoryOption object to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def categoryOption, ApiVersion apiVersion = null) {

        log.debug ">>> create categoryOption: " + categoryOption

        // remove the id
        categoryOption.remove('id')

        def result = apiService.post(auth, PATH, categoryOption, [:], ContentType.JSON, apiVersion)

        log.debug "<<< create categoryOption, result: " + result

        return result
    }


}
