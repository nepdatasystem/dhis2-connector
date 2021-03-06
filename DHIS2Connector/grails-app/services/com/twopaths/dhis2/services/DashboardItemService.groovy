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
 * Service to do DashboardItem CRUD with the DHIS 2 API
 */
@Transactional
class DashboardItemService {

    final def PATH = "/dashboardItems"

    def apiService

    /**
     * Finds all dashboard items that contain the supplied reportTable id
     *
     * @param auth DHIS 2 credentials
     * @param reportTableId Id of the report table to find dashboard items for
     * @param fields dashboard item fields to return
     * @param apiVersion DHIS 2 api version
     * @return found dashboard items if any
     */
    def findByReportTableId(def auth, def reportTableId, ArrayList<String> fields = [],
                            ApiVersion apiVersion = null) {

        def queryParams = [filter: "reportTable.id:eq:${reportTableId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }

    /**
     * Finds all dashboard items that contain the supplied map id
     *
     * @param auth DHIS 2 credentials
     * @param mapId Id of the map to find dashboard items for
     * @param fields dashboard item fields to return
     * @param apiVersion DHIS 2 api version
     * @return found dashboard items if any
     */
    def findByMapId(def auth, def mapId, ArrayList<String> fields = [],
                            ApiVersion apiVersion = null) {

        def queryParams = [filter: "map.id:eq:${mapId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }


    /**
     * Finds all dashboard items that contain the supplied chart id
     *
     * @param auth DHIS 2 credentials
     * @param chartId Id of the chart to find dashboard items for
     * @param fields dashboard item fields to return
     * @param apiVersion DHIS 2 api version
     * @return found dashboard items if any
     */
    def findByChartId(def auth, def chartId, ArrayList<String> fields = [],
                    ApiVersion apiVersion = null) {

        def queryParams = [filter: "chart.id:eq:${chartId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }

    /**
     * Finds Dashboard Items based off supplied query params
     *
     * @param auth DHIS 2 credentials
     * @param query Map of query parameters to use to find dashboard items
     * @param apiVersion ApiVersion to use
     * @return Dashboard Items found
     */
    def find (def auth, def query = [:], ApiVersion apiVersion = null) {

        def dashboardItems = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data?.dashboardItems

        log.debug "dashboardItems: " + dashboardItems

        return dashboardItems

    }

    /**
     * Deletes a dashboard item
     *
     * @param auth DHIS 2 credentials
     * @param dashboardItemId Id of the dashboard item to delete
     * @param apiVersion version of the DHIS 2 API to use
     * @return the Result of the deletion
     */
    def delete(def auth, def dashboardItemId, ApiVersion apiVersion = null) {

        log.debug ">>> dashboard item: " + dashboardItemId

        def path = "${PATH}/${dashboardItemId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< dashboard item, result: " + result

        return result
    }

}
