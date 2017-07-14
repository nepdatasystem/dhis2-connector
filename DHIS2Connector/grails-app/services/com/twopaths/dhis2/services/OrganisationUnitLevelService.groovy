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

/**
 * Service to do OrganisationUnit CRUD with the DHIS 2 API
 */
@Transactional
class OrganisationUnitLevelService {

    final def PATH = "/organisationUnitLevels"

    def apiService

    /**
     * Creates a lookup map of OrganisationUnitLevel names by the organisationUnitLevel's level
     *
     * @param auth DHIS 2 Credentials
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return A lookup map of OrganisationUnitLevel names by level
     */
    def getLookup(def auth, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        // Get all the organisation units
        def organisationUnitLevels = apiService.get(auth, "${PATH}", queryParams, null,
                apiVersion)?.data?.organisationUnitLevels

        // Create the lookup from the organization unit Levels. Lookup by level to get the name
        organisationUnitLevels.each { organisationUnitLevel ->
            lookup << [("${organisationUnitLevel.level}".toString()): ("${organisationUnitLevel.name}".toString())]
        }

        lookup = lookup.sort {it.key}

        return lookup
    }

    /**
     * Retrieves a single organisationUnitLevel by its name
     *
     * @param auth DHIS 2 Credentials
     * @param name Name to find the organisationUnitLevel for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The organisationUnitLevel found if any
     */
    def getByName(def auth, def name, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${name}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def organisationUnitLevels = apiService.get(auth, "${PATH}", queryParams, null,
                apiVersion)?.data?.organisationUnitLevels

        def orgUnitLevel

        if (organisationUnitLevels.size() == 1) {
            orgUnitLevel = organisationUnitLevels[0]
        }

        log.debug "orgUnitLevel: " + orgUnitLevel

        return orgUnitLevel
    }
}
