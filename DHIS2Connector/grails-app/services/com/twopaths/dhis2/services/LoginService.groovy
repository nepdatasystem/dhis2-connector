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

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import org.apache.http.conn.HttpHostConnectException
import org.springframework.security.authentication.AuthenticationServiceException

/**
 * This class handles logging into DHIS 2
 */
class LoginService {

    final def LOGIN_URL = "/dhis-web-commons-security/login.action"

    def server
    def context

    /**
     * Authenticates in DHIS 2 with the specified username and password
     * @param username Username to login with
     * @param password Password to login with
     * @return if the login was successful or not (boolean)
     */
    def authenticate(def username, def password) {

        def http = new HTTPBuilder(server)

        def postBody = [authOnly: true, j_username: username, j_password: password] // will be url-encoded

        def authenticated = false

        // Handler for successful login
        http.handler."200" = { HttpResponseDecorator resp, reader ->
            log.debug("Login successful")
            authenticated = true
        }

        // Handler for failed login
        http.handler."302" = { HttpResponseDecorator resp, reader ->
            log.debug "!!! 302 !!!"
            authenticated = false
        }

        http.handler.failure = { HttpResponseDecorator resp, reader ->
            log.debug "!!! failure !!!"
            authenticated = false
            def rsp = resp
            def rdr = reader
        }

        try {
            // Attempt to login
            http.post(path: context + LOGIN_URL, body: postBody, requestContentType: groovyx.net.http.ContentType.URLENC) { resp ->
                log.debug "POST Success: ${resp.statusLine}"
                log.debug "resp: " + resp
            }
        } catch (Exception e) {
            log.error ("Unable to post login to DHIS 2", e)
            authenticated = false
            if (e instanceof HttpHostConnectException) {
                throw new AuthenticationDHIS2ConnectionException ("Unable to connect to DHIS 2 to authenticate")
            } else {
                throw new AuthenticationServiceException ("Unspecified authentication error")
            }
        }

        log.debug "authenticated: " + authenticated
        return authenticated
    }
}
