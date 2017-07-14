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
 * Service to do DataSet CRUD with the DHIS 2 API
 */
@Transactional
class DataSetService {

    final def PATH = "/dataSets"

    def apiService
    def messageSource
    def sqlViewService
    def propertiesService

    public final def FIELD_ID = "id"
    public final def FIELD_NAME = "name"
    public final def FIELD_SHORT_NAME = "shortName"
    public final def FIELD_USER = "user[id,name]"
    public final def FIELD_CREATED = "created"
    public final def FIELD_LAST_UPDATED = "lastUpdated"
    public final def FIELD_DATA_SET_ELEMENTS = "dataSetElements"
    public final def FIELD_DATA_ELEMENTS_CATEGORY_COMBO = "[id,dataElement[id,categoryCombo[name,categoryOptionCombos::isNotEmpty]]"

    /**
     * Creates a DataSet via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param dataSet The DataSet object to create
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def create(def auth, def dataSet, ApiVersion apiVersion = null) {

        log.debug ">>> dataSet: " + dataSet

        def result = apiService.post(auth, PATH, dataSet, [:], ContentType.JSON, apiVersion)

        log.debug "<<< dataSet, result: " + result

        return result
    }

    /**
     * Deletes a data set
     *
     * @param auth DHIS 2 Credentials
     * @param dataSetId The Id of the data set to delete
     * @return The parsed Result object from the API
     */
    def delete(def auth, def dataSetId, ApiVersion apiVersion = null) {
        log.debug ">>> dataSet: " + dataSetId

        def path = "${PATH}/${dataSetId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< dataSet, result: " + result

        return result

    }

    /**
     * Finds all DataSets in the system
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The full list of DataSets
     */
    def findAll(def auth, ArrayList<String> fields = [], ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataSets = apiService.get(auth, PATH, queryParams, null, apiVersion)?.data?.dataSets

        log.debug "<<< dataSets: " + dataSets

        return dataSets
    }

    /**
     * Returns all DataSets in the system, with additional calculated attributes:
     * - hasMetadata
     * - hasDisaggregations
     * - hasData
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The full list of DataSets with reload info appended
     */
    def findAllWithReuploadInfo(def auth, ApiVersion apiVersion = null) {

        def dataSetFields = [
                FIELD_ID,
                FIELD_NAME,
                FIELD_SHORT_NAME,
                FIELD_USER,
                FIELD_CREATED,
                FIELD_LAST_UPDATED,
                FIELD_DATA_SET_ELEMENTS + FIELD_DATA_ELEMENTS_CATEGORY_COMBO
        ]

        def dataSets = findAll(auth, dataSetFields, apiVersion)

        def dataSetsWithData = findDataSetsWithData(auth, apiVersion)?.flatten()

        dataSets.each { dataSet ->
            dataSet.hasMetadata = dataSet.dataSetElements?.size() > 0
            dataSet.hasDisaggregations = false
            if (dataSet.hasMetadata) {
                dataSet.dataSetElements.find { dataSetElement ->
                    if (dataSetElement?.dataElement?.categoryCombo?.name != "default") {
                        dataSet.hasDisaggregations = dataSetElement.dataElement.categoryCombo?.categoryOptionCombos
                        return true
                    }
                }
            }
            dataSet.hasData = dataSet.id in dataSetsWithData
        }
        return dataSets
    }

    /**
     * Returns a list of all DataSets that have associated data
     *
     * @param auth DHIS 2 Credentials
     * @param criteria Map of criteria to find DataSets for
     * @param apiVersion ApiVersion to use
     * @return A list of all DataSets that have associated data
     */
    def findDataSetsWithData (def auth, def criteria = [:], ApiVersion apiVersion = null) {

        def sqlViewName = propertiesService.getProperties().getProperty('nep.sqlview.datasets.with.data.name', null)

        if (!sqlViewName) {
            throw new Exception("No application property specified for 'nep.sqlview.datasets.with.data.name'. " +
                    "Please create this property and create corresponding view in DHIS 2, then restart the application")
        }

        def sqlView = sqlViewService.findByName(auth, sqlViewName, apiVersion)

        if (!sqlView) {
            throw new Exception("Unable to find sql view with name ${sqlViewName}. Please ensure this has been created in DHIS 2")
        }

        // need to execute the view first in case the actual underlying db view was deleted
        sqlViewService.executeView(auth, sqlView.id, apiVersion)

        return sqlViewService.getViewData(auth, sqlView.id, criteria, apiVersion)
    }

    /**
     * Determines if the specified Data Set has data or not
     *
     * @param auth DHIS 2 credentials
     * @param dataSetID The id of the data set to check for data
     * @param apiVersion DHIS 2 api version
     * @return if the data set has data or not
     */
    boolean dataSetHasData (def auth, def dataSetID, ApiVersion apiVersion = null) {
        def dbRows = findDataSetsWithData(auth, ["uid" : dataSetID], apiVersion)

        return (dbRows?.size() > 0)
    }

    /**
     * Retrieves the specified DataSet from the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the DataSet to retrieve
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The DataSet found if any
     */
    def get(def auth, def id, ArrayList<String> fields = [], ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def dataSet = apiService.get(auth, "${PATH}/${id}", queryParams, null,
                apiVersion)?.data

        log.debug "dataSet: " + dataSet

        return dataSet
    }

    /**
     * Returns a map of DHIS 2 Period Frequencies.
     * - Key: DHIS 2 Frequency Value
     * - Value: translated User Friendly label for the frequency
     *
     * @return A Map of DHIS 2 Period Frequencies
     */
    def getFrequencies() {

        return [
                Daily : messageSource.getMessage("dataset.frequency.Daily", null, Locale.default),
                Weekly : messageSource.getMessage("dataset.frequency.Weekly", null, Locale.default),
                Monthly : messageSource.getMessage("dataset.frequency.Monthly", null, Locale.default),
                BiMonthly : messageSource.getMessage("dataset.frequency.BiMonthly", null, Locale.default),
                Quarterly : messageSource.getMessage("dataset.frequency.Quarterly", null, Locale.default),
                SixMonthly : messageSource.getMessage("dataset.frequency.SixMonthly", null, Locale.default),
                SixMonthlyApril : messageSource.getMessage("dataset.frequency.SixMonthlyApril", null, Locale.default),
                Yearly : messageSource.getMessage("dataset.frequency.Yearly", null, Locale.default),
                FinancialApril : messageSource.getMessage("dataset.frequency.FinancialApril", null, Locale.default),
                FinancialJuly : messageSource.getMessage("dataset.frequency.FinancialJuly", null, Locale.default),
                FinancialOct : messageSource.getMessage("dataset.frequency.FinancialOct", null, Locale.default)
        ]
    }

    /**
     * Returns a map of DHIS 2 Open Future Period values
     *
     * - Key: DHIS 2 open future period value
     * -- 0 represents no open future periods allowed
     * -- 12 represents open future periods allowed
     * - Value: UI label which is yes/no
     *
     * @return Map of DHIS 2 Open Future Period values
     */
    def getOpenFuturePeriods() {

        return [
                "0" : messageSource.getMessage("label.no", null, Locale.default),
                "12" : messageSource.getMessage("label.yes", null, Locale.default)
        ]
    }
}
