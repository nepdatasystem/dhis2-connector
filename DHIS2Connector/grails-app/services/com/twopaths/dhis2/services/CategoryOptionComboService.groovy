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
 * Service to do CategoryOptionCombo CRUD with the DHIS 2 API
 */
@Transactional
class CategoryOptionComboService {

    final def PATH = "/categoryOptionCombos"
    final def CATEGORY_OPTIONS_SUB_PATH = "categoryOptions"

    def apiService

    /**
     * Retrieves the specified CategoryOptionCombo from the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the CategoryOptionCombo to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryOptionCombo found if any
     */
    def get(def auth, def id, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryOptionCombo = apiService.get(auth, "${PATH}/${id}", queryParams, null,
                apiVersion)?.data

        log.debug "categoryOptionCombo: " + categoryOptionCombo

        return categoryOptionCombo
    }

    /**
     * Finds all CategoryOption Combos with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name The name to retrieve CategoryOptionCombos for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryOptionCombo found if any
     */
    def findByName(def auth, def name, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryOptionCombos = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.categoryOptionCombos

        log.debug "categoryOptionCombos: " + categoryOptionCombos

        def categoryOptionCombo
        if (categoryOptionCombos.size() == 1) {
            categoryOptionCombo = categoryOptionCombos[0]
        }

        log.debug "categoryOptionCombo: " + categoryOptionCombo

        return categoryOptionCombo
    }

    /**
     * Finds all CategoryOptionCombos in the system
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The full list of CategoryOptionCombos
     */
    def findAll(def auth, ApiVersion apiVersion = null) {

        def categoryOptionCombos = apiService.get(auth, "${PATH}", [:], null, apiVersion)?.data

        log.debug "categoryOptionCombos: " + categoryOptionCombos

        return categoryOptionCombos
    }

    /**
     * Creates a categoryOptionCombo
     *
     * @param auth DHIS 2 Credentials
     * @param categoryOptionCombo The categoryOptionCombo object to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def categoryOptionCombo, ApiVersion apiVersion = null) {

        log.debug ">>> create categoryOptionCombo: " + categoryOptionCombo

        // remove the id
        categoryOptionCombo.remove('id')

        def result = apiService.post(auth, PATH, categoryOptionCombo, [:], ContentType.JSON, apiVersion)

        log.debug "<<< create categoryOptionCombo. result: " + result

        return result

    }

    /**
     * Assigns the specified CategoryOption to the specified CategoryOptionCombo
     *
     * @param auth DHIS 2 Credentials
     * @param categoryOptionComboId The Id of the CategoryOptionCombo to assign the CategoryOption to
     * @param categoryOptionId The Id of the CategoryOption to assign to the CategoryOptionCombo
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignCategoryOptionToCategoryOptionCombo(def auth, def categoryOptionComboId, def categoryOptionId,
                                                  ApiVersion apiVersion = null) {

        log.debug ">>> categoryOptionComboId: " + categoryOptionComboId

        log.debug "${PATH}/${categoryOptionComboId}/${CATEGORY_OPTIONS_SUB_PATH}/${categoryOptionId}"

        def result = apiService.post(auth, "${PATH}/${categoryOptionComboId}/${CATEGORY_OPTIONS_SUB_PATH}/${categoryOptionId}",
                null, [:], ContentType.JSON, apiVersion )

        return result
    }

}
