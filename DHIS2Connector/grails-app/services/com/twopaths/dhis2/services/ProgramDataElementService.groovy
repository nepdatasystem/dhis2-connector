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

import com.twopaths.dhis2.api.ApiStrategy
import com.twopaths.dhis2.api.ApiVersion
import grails.transaction.Transactional
import groovyx.net.http.ContentType

/**
 *  Service to do Program Data Element CRUD with the DHIS 2 API
 */
@Transactional
class ProgramDataElementService {

    final def PATH = "/programDataElements"
    
    def apiService

    /**
     * Deletes  the specified program data element
     *
     * @param auth DHIS 2 credentials
     * @param programDataElementId The id of the program data element to delete
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def programDataElementId, ApiVersion apiVersion = null) {

        def path = "${PATH}/${programDataElementId}"

        log.debug "programDataElement.delete"

        def result = apiService.delete (auth, path, [:], ContentType.JSON, apiVersion)

        return result
    }

    /**
     * Finds all Program Data Elements according to the specified query params
     *
     * @param auth DHIS 2 credentials
     * @param queryParams Map of query parameters to use for the search
     * @param apiVersion DHIS 2 api version
     * @return found Program Data Elements if any
     */
    def find (def auth, queryParams = [:], ApiVersion apiVersion = null) {

        log.debug "programDataElements.find"

        def programDataElements = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.programDataElements

        return programDataElements
    }
}
