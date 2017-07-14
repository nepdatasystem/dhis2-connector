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
 * Service to do DataElement CRUD with the DHIS 2 API
 */
@Transactional
class DataElementService {

    final def PATH = "/dataElements"
    final def DATASET_SUB_PATH = "dataSets"

    public final String ALL_FIELDS = ":all"
    public final String FIELD_VALUE_TYPE = "valueType"
    public final String FIELD_CATEGORY_COMBO = "categoryCombo[:identifiable]"
    public final String FIELD_ID = "id"

    def apiService

    /**
     * Creates a DataElement
     *
     * @param auth DHIS 2 Credentials
     * @param dataElement The DataElement object to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def dataElement, ApiVersion apiVersion = null) {

        log.debug ">>> dataElement: " + dataElement

        // remove the id
        dataElement.remove('id')

        def result = apiService.post(auth, PATH, dataElement, [:], ContentType.ANY, apiVersion)

        log.debug "<<< result: " + result

        return result
    }

    /**
     * Updates a DataElement
     *
     * @param auth DHIS 2 Credentials
     * @param dataElement The DataElement object to update
     * @param mergeMode The ApiMergeMode to use for this update
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def update(def auth, def dataElement, ApiMergeMode mergeMode = ApiMergeMode.REPLACE,
               ApiVersion apiVersion = null) {

        def query = [mergeMode: mergeMode.value()]

        def result = apiService.put(auth, PATH, dataElement, dataElement.id, query, ContentType.JSON, apiVersion)

        log.debug "update, result: " + result

        return result
    }

    /**
     * Retrieves the specified Data Element from the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the DataElement to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The DataElement found if any
     */
    def get(def auth, def id, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataElement = apiService.get(auth, "${PATH}/${id}", queryParams, null,
                apiVersion)?.data

        log.debug "dataElement: " + dataElement

        return dataElement
    }

    /**
     * Finds all DataElements with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name The name to retrieve DataElements for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The DataElement found if any
     */
    def findByName(def auth, def name, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]
        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataElements = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.dataElements

        log.debug "dataElements: " + dataElements

        def dataElement
        if (dataElements.size() == 1) {
            dataElement = dataElements[0]
        }


        return dataElement
    }

    /**
     * Finds all DataElements with the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to retrieve DataElements for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The DataElement found if any
     */
    def findByCode(def auth, def code, ArrayList<String> fields = [":all"],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "code:eq:${code}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataElements = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.dataElements

        log.debug "dataElements: " + dataElements

        def dataElement
        if (dataElements.size() == 1) {
            dataElement = dataElements[0]
        }


        return dataElement
    }

    /**
     * Finds all DataElements with the specified UID
     *
     * @param auth DHIS 2 Credentials
     * @param UID The UID to retrieve DataElements for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The DataElement found if any
     */
    def findByUID(def auth, def UID, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataElement = apiService.get(auth, "$PATH/${UID}", queryParams, null, apiVersion)

        return dataElement
    }

    /**
     * Produces a lookup map of all DataElements by code
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion
     * @return A lookup map of all DataElements by code
     */
    def getLookup(def auth, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allDataElements = []

        def dataElements = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        if (dataElements) {
            allDataElements.addAll(dataElements.dataElements)

            // Create the lookup from the data elements
            allDataElements.each { dataElement ->
                lookup << [("${dataElement.code}".toString()): dataElement]
            }
        }
        return lookup
    }

    /**
     * Assigns the specified DataSet to the specified DataElement
     *
     * @param auth DHIS 2 Credentials
     * @param dataElementId The Id of the DataElement to assign the DataSet to
     * @param dataSetId The Id of the DataSet to assign to the DataElement
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignDataSetToDataElement(def auth, def dataElementId, def dataSetId,
                                   ApiVersion apiVersion = null) {

        log.debug ">>> dataElement: " + dataElementId

        def json = apiService.post(auth, "${PATH}/${dataElementId}/${DATASET_SUB_PATH}/${dataSetId}",
                null, [:], ContentType.JSON, apiVersion )

        return json
    }

}
