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
 * Service to do UserRole CRUD with the DHIS 2 API
 */
@Transactional
class UserRoleService {

    final def PATH = "/userRoles"
    final def DATASET_SUBPATH="dataSets"
    final def PROGRAM_SUBPATH="programs"

    def apiService

    /**
     * Finds the userRole with the specified name (role)
     *
     * @param auth DHIS 2 Credentials
     * @param role The name of the role to find the UserRole for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return The UserRole found if any
     */
    def findByRole(def auth, def role, ArrayList<String> fields = [],
                   ApiVersion apiVersion = null) {

        def queryParams = [filter: "name:eq:${role}"]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def userRoles = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data?.userRoles

        log.debug "userRoles: " + userRoles

        // Should only be one role....get that and return it
        if (userRoles?.size() == 1) {
            return userRoles[0]
        } else {
            return null
        }
    }

    /**
     * Finds all user roles that match the list of specified role names
     *
     * @param auth DHIS 2 Credentials
     * @param roles List of role names to find the UserRole objects for
     * @param fields Fields requested in the response from the API
     * @param apiVersion ApiVersion to use
     * @return List of UserRoles found
     */
    def findByRoles(def auth, def roles, ArrayList<String> fields = [],
                    ApiVersion apiVersion = null) {

        // Need to use a queryString for the filter

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def queryString = ""
        roles.each { role ->
            def encodedRole = java.net.URLEncoder.encode(role, "UTF-8")
            queryString += "&filter=name:eq:${encodedRole}"
        }
        if (queryString) {
            queryString = queryString.substring(1)
        }

        def userRoles = apiService.get(auth, "${PATH}", [:], queryString, apiVersion)?.data?.userRoles

        log.debug "userRoles: " + userRoles
        return userRoles
    }

    /**
     * Assigns a data set to the specified user role
     *
     * @param auth DHIS 2 Credentials
     * @param userRole The user role to assign the data set to
     * @param dataSetId The id of the data set to assign to the user role
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignDataSetToUserRole(def auth, def userRole, def dataSetId,
                                ApiVersion apiVersion = null) {

        def result = apiService.post(auth, "${PATH}/${userRole.id}/${DATASET_SUBPATH}/${dataSetId}")

        return result
    }

    /**
     * Assigns a program to the specified user role
     * @param auth DHIS 2 Credentials
     * @param userRole The user role to assign the program to
     * @param programId The id of the program to assign to the user role
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def assignProgramToUserRole(def auth, def userRole, def programId,
                                ApiVersion apiVersion = null) {

        def result = apiService.post(auth, "${PATH}/${userRole.id}/${PROGRAM_SUBPATH}/${programId}",
                null, [:], ContentType.JSON, apiVersion)

        return result
    }

}
