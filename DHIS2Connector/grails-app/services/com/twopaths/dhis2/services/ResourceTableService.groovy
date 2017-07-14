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
import org.apache.http.HttpStatus

/**
 * Service to kick off jobs related to resource tables and analytics in the DHIS 2 API
 */
@Transactional
class ResourceTableService {

    final def PATH = "/resourceTables"
    final def ANALYTICS_SUBPATH = "analytics"

    final String RESOURCE_TABLE_SUCCESS_MESSAGE = "Initiated resource table update"
    final String ANALYTICS_TABLE_SUCCESS_MESSAGE = "Initiated analytics table update"

    def apiService

    /**
     * Generates DHIS 2 resource tables
     * Within DHIS 2, the resource tables are also refreshed when generating the analytics tables, so no need to call
     * this one directly if calling the generateAnalyticsTables
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def generateResourceTables (def auth, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, [], [:], ContentType.JSON, apiVersion)

        // TODO: this response is non-standard. We only know it was successful when the Http Status = 200 and
        // the message = "Initiated resource table update"
        // Need to update the response success to reflect this, until this response is changed in DHIS 2
        log.debug "generateResourceTables Response: " + result

        if (result?.status == HttpStatus.SC_OK && result?.message == RESOURCE_TABLE_SUCCESS_MESSAGE) {
            result?.success = true
        }

        return result
    }

    /**
     * Generates the analytics tables
     * This also regenerates the resource tables too
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the creation
     */
    def generateAnalyticsTables (def auth, ApiVersion apiVersion = null) {

        def result = apiService.post(auth, "${PATH}/${ANALYTICS_SUBPATH}", [], [:], ContentType.JSON, apiVersion)

        // TODO: this response is non-standard. We only know it was successful when the Http Status = 200 and
        // the message = "Initiated analytics table update"
        // Need to update the response success to reflect this, until this response is changed in DHIS 2
        log.debug "generateAnalyticsTables Response: " + result

        if (result?.status == HttpStatus.SC_OK && result?.message == ANALYTICS_TABLE_SUCCESS_MESSAGE) {
            result?.success = true
        }

        return result
    }

}
