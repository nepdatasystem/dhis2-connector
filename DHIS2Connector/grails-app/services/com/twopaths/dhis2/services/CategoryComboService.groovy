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
 * Service to do CategoryCombo CRUD with the DHIS 2 API
 */
@Transactional
class CategoryComboService {

    final def PATH = "/categoryCombos"
    final def CATEGORY_SUB_PATH = "categories"

    public final String DEFAULT_CATEGORY_COMBO_NAME = "default"

    public final String FIELD_CATEGORY_OPTION_COMBOS = "categoryOptionCombos[:identifiable]"

    private static def defaultCategoryComboId

    def apiService

    /**
     * Retrieves the specified CategoryCombo from the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the CategoryCombo to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryCombo found if any
     */
    def get(def auth, def id, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryCombo = apiService.get(auth, "${PATH}/${id}", queryParams, null,
                apiVersion)?.data

        log.debug "categoryCombo: " + categoryCombo

        return categoryCombo
    }

    /**
     * Finds all CategoryCombos with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name The name to retrieve CategoryCombos for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The CategoryCombo found if any
     */
    def findByName(def auth, def name, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def categoryCombos = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.categoryCombos

        log.debug "categoryCombos: " + categoryCombos

        def categoryCombo
        if (categoryCombos.size() == 1) {
            categoryCombo = categoryCombos[0]
        }

        log.debug "categoryCombo: " + categoryCombo

        return categoryCombo
    }

    /**
     * Finds all CategoryCombos in the system
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The full list of CategoryCombos
     */
    def findAll(def auth, ApiVersion apiVersion = null) {

        def categoryCombos = apiService.get(auth, "${PATH}", [:], null, apiVersion)?.data

        log.debug "categoryCombos: " + categoryCombos

        return categoryCombos
    }

    /**
     * Creates a categoryCombo
     *
     * @param auth DHIS 2 Credentials
     * @param categoryCombo The categoryCombo object to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def categoryCombo, ApiVersion apiVersion = null) {

        log.debug ">>> categoryCombo: " + categoryCombo

        // remove the id
        categoryCombo.remove('id')

        def result = apiService.post(auth, PATH, categoryCombo, [:], ContentType.JSON, apiVersion)

        log.debug "<<< categoryCombo result: " + result

        return result

    }

    /**
     * Assigns the specified Category to the specified CategoryCombo
     *
     * @param auth DHIS 2 Credentials
     * @param categoryComboId The Id of the CategoryCombo to assign the Category to
     * @param categoryId The Id of the Category to assign to the CategoryCombo
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignCategoryToCategoryCombo(def auth, def categoryComboId, def categoryId,
                                      ApiVersion apiVersion = null) {

        log.debug ">>> categoryComboId: " + categoryComboId

        def result = apiService.post(auth, "${PATH}/${categoryComboId}/${CATEGORY_SUB_PATH}/${categoryId}", null, [:],
                ContentType.JSON, apiVersion )

        return result
    }

    /**
     * Retrieves the Id of the DHIS 2 default category combo.
     * This often needs to be used to post with objects that do not explicitly have a category combo set.
     *
     * @param auth DHIS 2 Credentials
     * @param cached Whether this should retrieve the cached version or if it should be forced to refresh from DHIS 2
     * @param apiVersion ApiVersion to use
     * @return The Id of the default category combo
     */
    def getDefaultCategoryComboId (def auth, boolean cached = true,
                                   ApiVersion apiVersion = null) {
        // only need to set this the very first time, the default will never change
        // but give consumer option to retrieve regardless
        // this will cut down on API calls by the consumer
        // the default category combo needs to be set on a new dataset and new program
        if (!defaultCategoryComboId || !cached) {

            defaultCategoryComboId = findByName(auth, DEFAULT_CATEGORY_COMBO_NAME, ["id"], apiVersion)?.id
        }
        return defaultCategoryComboId

    }
}
