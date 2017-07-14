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

import grails.transaction.Transactional
import com.twopaths.dhis2.api.ApiVersion
import groovyx.net.http.ContentType

/**
 * Service to do Report Table CRUD with the DHIS 2 API
 */
@Transactional
class ReportTableService {

    final def PATH = "/reportTables"

    def apiService

    /**
     * Finds all report tables that contain the supplied programDataElement
     *
     * @param auth DHIS 2 credentials
     * @param programDataElementId Id of the program data element to find report tables for
     * @param fields Report Table fields to return
     * @param apiVersion DHIS 2 api version
     * @return found report tables if any
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
     * Finds all report tables that contain the supplied programDataElement
     *
     * @param auth DHIS 2 credentials
     * @param programAttributeId Id of the program attribute to find report tables for
     * @param fields Report table fields to return
     * @param apiVersion DHIS 2 api version
     * @return found report tables if any
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
     * Finds Report Tables based off supplied query params
     *
     * @param auth DHIS 2 credentials
     * @param query Map of query paramaters to use
     * @param apiVersion DHIS 2 api version
     * @return reportTables found
     */
    def find (def auth, def query = [:], ApiVersion apiVersion = null) {

        def reportTables = apiService.get(auth, "${PATH}", query, null, apiVersion)?.data?.reportTables

        log.debug "reportTables: " + reportTables

        return reportTables

    }

    /**
     * Deletes a report table (favourite)
     *
     * @param auth DHIS 2 credentials
     * @param reportTableId Id of the report table to delete
     * @param apiVersion version of the DHIS 2 API to use
     * @return the Result of the deletion
     */
    def delete(def auth, def reportTableId, ApiVersion apiVersion = null) {

        log.debug ">>> reportTable: " + reportTableId

        def path = "${PATH}/${reportTableId}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< reportTable, result: " + result

        return result

    }

}
