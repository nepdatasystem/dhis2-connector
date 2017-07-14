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
 *
 * Service to do DataValue CRUD with the DHIS 2 API
 */
@Transactional
class DataValueService {

    final def PATH = "/dataValueSets"

    def apiService

    /**
     * Creates a data value via the API
     *
     * @param auth DHIS 2 Credentials
     * @param dataValue The Data Value to create
     * @param apiVersion ApiVersion to use
     * @return the Result of the data value creation
     */
    def create(def auth, def dataValue, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, dataValue, [:], ContentType.JSON, apiVersion)

        log.debug "<<< dataValue: " + dataValue

        return result

    }

    /**
     * Retrieves data values matching the supplied criteria
     *
     * @param auth DHIS 2 Credentials
     * @param dataSetId Id of the data set to retrieve data values for
     * @param orgUnitId Id of the org unit to retrieve data values for
     * @param limit Max number of records to retrieve
     * @param startDate Min date to retrieve data values for
     * @param endDate Max date to retrieve data values for
     * @param children Whether or not to include children in the records retrieved
     * @param apiVersion ApiVersion to use
     * @return dataValues found
     */
    def read (def auth, def dataSetId, def orgUnitId, def limit = null,
              def startDate = "1970-01-01", def endDate="2100-01-01", def children = true,
              ApiVersion apiVersion = null) {

        def queryParams = [
                dataSet: dataSetId,
                orgUnit: orgUnitId,
                startDate: startDate,
                endDate: endDate,
                children: children
        ]

        if (limit && limit > 0) {
            queryParams.put("limit", limit)
        }

        def response = apiService.get(auth, PATH, queryParams, null, apiVersion)?.data

        def dataValues = response?.dataValues

        return dataValues
    }
}
