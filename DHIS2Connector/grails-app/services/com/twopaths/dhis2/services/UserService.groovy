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
 * Service to do User CRUD with the DHIS 2 API
 */
@Transactional
class UserService {

    final def PATH_CURRENT_USER = "/me"
    final def USER_CREDENTIALS_ROLES_FIELDS = "userCredentials[userRoles[name]]"

    def apiService

    /**
     * Find the roles for the supplied username
     *
     * @param auth DHIS 2 Credentials
     * @param apiVersion ApiVersion to use
     * @return UserRoles associated with the associated user credentials
     */
    def findUserRoles(def auth, ApiVersion apiVersion = null) {

        def queryParams = [fields: USER_CREDENTIALS_ROLES_FIELDS]

        // has not been implemented in DHIS 2 core. Have contacted DHIS 2 developers to fix.
        def userCredentials = apiService.get(auth, "${PATH_CURRENT_USER}", queryParams, null,
                apiVersion)?.data

        log.debug "userCredentials: " + userCredentials

        def userRoles

        if (userCredentials?.userCredentials?.userRoles) {

            userRoles = userCredentials.userCredentials.userRoles?.collect { userRole -> userRole.name }
        }
        log.debug "userRoles: " + userRoles

        return userRoles
    }
}
