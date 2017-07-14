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
 * Service to do Chart CRUD with the DHIS 2 API
 */
@Transactional
class ChartService {

    final def PATH = "/charts"

    def apiService

    /**
     * Finds all charts that contain the supplied programDataElement
     *
     * @param auth DHIS 2 credentials
     * @param programDataElementId Id of the program data element to find charts for
     * @param fields Chart fields to return
     * @param apiVersion DHIS 2 api version
     * @return found charts if any
     */
    def findByProgramDataElementId (def auth, def programDataElementId, ArrayList<String> fields = [],
                                  ApiVersion apiVersion = null) {

        def queryParams = [filter: "dataDimensionItems.programDataElement.id:eq:${programDataElementId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }

    /**
     * Finds all charts that contain the supplied programDataElement
     *
     * @param auth DHIS 2 credentials
     * @param programAttributeId Id of the program attribute to find charts for
     * @param fields Chart fields to return
     * @param apiVersion DHIS 2 api version
     * @return found charts if any
     */
    def findByProgramAttributeId (def auth, def programAttributeId, ArrayList<String> fields = [],
                                  ApiVersion apiVersion = null) {

        def queryParams = [filter: "dataDimensionItems.programAttribute.id:eq:${programAttributeId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }

    /**
     * Finds Charts based off supplied query params
     *
     * @param auth DHIS 2 credentials
     * @param query Map of query paramaters to use
     * @param apiVersion DHIS 2 api version
     * @return charts found
     */
    def find (def auth, def query = [:], ApiVersion apiVersion = null) {

        def charts = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data?.charts

        log.debug "charts: " + charts

        return charts

    }

    /**
     * Deletes a chart
     *
     * @param auth DHIS 2 credentials
     * @param chartId Id of the chart to delete
     * @param apiVersion version of the DHIS 2 API to use
     * @return the Result of the deletion
     */
    def delete(def auth, def chartId, ApiVersion apiVersion = null) {

        log.debug ">>> chart: " + chartId

        def path = "${PATH}/${chartId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< chart, result: " + result

        return result

    }

}
