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
 * This class handles calls to the DHIS 2 Metadata Service
 */
@Transactional
class MetadataService {

    final def PATH = "/metadata"

    def apiService

    /*
     * Creates or updates (POSTs) objects via the DHIS 2 Metadata API
     *
     * Possible versions for the metadata API as of v 2.24:
     * - ApiVersion.DHIS2_DEFAULT_VERSION
     * - ApiVersion.DHIS2_VERSION_223
     * - ApiVersion.DHIS2_VERSION_224
     *
     * @param auth DHIS 2 Credentials
     * @param metadata The metadata to post
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return the Result of the API update
     */
    def createOrUpdate(def auth, def metadata, def query = [:], ApiVersion apiVersion = null) {

        log.debug ">>> create metadata: " + metadata

        def result = apiService.post(auth, PATH, metadata, query, ContentType.JSON, apiVersion)

        log.debug "<<< createOrUpdate metadata, result: " + result

        return result
    }
}
