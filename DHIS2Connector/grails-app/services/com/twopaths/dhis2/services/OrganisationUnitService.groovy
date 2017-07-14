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
 * Service to do OrganisationUnit CRUD with the DHIS 2 API
 */
@Transactional
class OrganisationUnitService {

    final def PATH = "/organisationUnits"

    def apiService

    /**
     * Creates a lookup map of OrganisationUnitIds by the organisationUnit's code in lower case
     *
     * @param auth DHIS 2 Credentials
     * @param fields array of fields to return in the OrganisationUnits
     * @param apiVersion ApiVersion to use
     * @return a lookup map of organisationUnit ids by code
     */
    def getLookup(def auth, ArrayList<String> fields = ["id", "code"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allOrganisationUnits = []

        // Get the first page of organisation units
        def organisationUnits = apiService.get(auth, "${PATH}", queryParams, null,
                apiVersion)?.data?.organisationUnits

        if (organisationUnits) {
            allOrganisationUnits.addAll(organisationUnits)

            // Create the lookup from the organization units
            allOrganisationUnits.each { organisationUnit ->
                lookup << [("${organisationUnit.code}"?.toString()?.toLowerCase()): organisationUnit.id]
            }
        }

        return lookup
    }

    /**
     * Finds all organisationUnits for the specified organisationUnitLevel
     *
     * @param auth DHIS 2 Credentials
     * @param level The organisationUnitLevel to find organisationUnits for
     * @param fields array of fields to return in the OrganisationUnits
     * @param apiVersion ApiVersion to use
     * @return Found organisationUnits
     */
    def findByLevel(def auth, def level, ArrayList<String> fields = [],
                    ApiVersion apiVersion = null) {

        def queryParams = [filter: "level:eq:${level}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }


        def orgUnits = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.organisationUnits

        return orgUnits?.collect { orgUnit -> orgUnit.id }
    }

    /**
     * Gets a single organisationUnit with the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the organisationUnit for
     * @param fields array of fields to return in the OrganisationUnits
     * @param apiVersion ApiVersion to use
     * @return The found organisationUnit if any
     */
    def findByCode(def auth, def code, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "code:eq:${code}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def orgUnits = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.organisationUnits

        def orgUnit
        if (orgUnits.size() == 1) {
            orgUnit = orgUnits[0]
        }

        return orgUnit
    }

    /**
     * Retrieves an organisationUnit with the specified id
     *
     * @param auth DHIS 2 Credentials
     * @param id Id of the organisationUnit to retrieve
     * @param fields array of fields to return in the OrganisationUnits
     * @param apiVersion ApiVersion to use
     * @return The found organisationUnit if any
     */
    def get(def auth, def id, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def orgUnit = apiService.get(auth, "${PATH}/${id}", queryParams, null, apiVersion)?.data

        log.debug "orgUnit: " + orgUnit

        return orgUnit
    }

    /**
     * Updates an organisationUnit
     *
     * @param auth DHIS 2 Credentials
     * @param orgUnit The organisationUnit to update
     * @param query Map of query paramaters to use
     * @param apiVersion ApiVersion to use
     * @return the Result of the API update
     */
    def update(def auth, def orgUnit, def query = [:], ApiVersion apiVersion = null) {

        def result = apiService.put(auth, PATH, orgUnit, orgUnit.id, query, ContentType.JSON, apiVersion)

        return result

    }

}
