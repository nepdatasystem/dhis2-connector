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
 * Service to do Event Chart CRUD with the DHIS 2 API
 */
@Transactional
class EventChartService {

    final def PATH = "/eventCharts"

    def apiService

    /**
     * Finds all event charts that contain the supplied programId
     *
     * @param auth DHIS 2 credentials
     * @param programId Id of the program to find event charts for
     * @param fields EventChart fields to return
     * @param apiVersion DHIS 2 api version
     * @return found event charts if any
     */
    def findByProgramId(def auth, def programId, ArrayList<String> fields = [],
                        ApiVersion apiVersion = null) {

        def queryParams = [filter: "program.id:eq:${programId}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        return find(auth, queryParams, apiVersion)
    }


    /**
     * Finds Event Charts based off supplied query params
     *
     * @param auth DHIS 2 credentials
     * @param query map of query params
     * @param apiVersion DHIS 2 api version
     * @return event charts found
     */
    def find (def auth, def query = [:], ApiVersion apiVersion = null) {

        def eventCharts = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data?.eventCharts

        log.debug "event charts: " + eventCharts

        return eventCharts

    }

    /**
     * Deletes an event chart
     *
     * @param auth DHIS 2 credentials
     * @param eventChartId Id of the event chart to delete
     * @param apiVersion version of the DHIS 2 API to use
     * @return the Result of the deletion
     */
    def delete(def auth, def eventChartId, ApiVersion apiVersion = null) {

        log.debug ">>> event chart: " + eventChartId

        def path = "${PATH}/${eventChartId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< event chart, result: " + result

        return result

    }
}
