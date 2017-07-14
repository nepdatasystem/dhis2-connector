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
 * Service to do Indicator CRUD with the DHIS 2 API
 */
@Transactional
class IndicatorService {

    final def PATH = "/indicators"

    def apiService

    /**
     * Finds all Indicators containing references to the specified data element
     *
     * @param auth DHIS 2 credentials
     * @param dataElementID Id of the data element to find indicators for
     * @param fields Fields requested in the response from the API
     * @param apiVersion DHIS 2 api version
     * @return found Indicators if any
     */
    def findAllByDataElement(def auth, def dataElementID, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        // in order to find indicators that use the specified data element, have to search if the ID is included in
        // either the numerator or the denominator. Can use the logical operator of "OR" to join the 2 portions of the
        // query using the "rootJunction" functionality in DHIS 2
        // https://docs.dhis2.org/2.25/en/developer/html/webapi_metadata_object_filter.html#webapi_metadata_logical_operator
        def queryParams = [
                rootJunction: "OR"
        ]
        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        // have to send this as a queryString because the filter param is deliberately supplied twice and therefore
        // cannot be supplied as the key in the queryParams map as map keys need to be unique
        def queryString = "filter=numerator:like:${dataElementID}&filter=denominator:like:${dataElementID}".toString()

        def indicators = apiService.get(auth, "${PATH}", queryParams, queryString, apiVersion)?.data?.indicators

        log.debug "indicators: " + indicators

        return indicators
    }

    /**
     * Deletes the specified Indicator
     *
     * @param auth DHIS 2 credentials
     * @param indicatorID The id of the indicator to delte
     * @param apiVersion DHIS 2 api version
     * @return The parsed Result object
     */
    def delete (def auth, def indicatorID, ApiVersion apiVersion = null) {

        log.debug ">>> indicator: ${indicatorID}"

        def path = "${PATH}/${indicatorID}"

        def result = apiService.delete(auth, path, [:], ContentType.JSON, apiVersion)

        log.debug "<<< indicator, result: " + result

        return result
    }
}
