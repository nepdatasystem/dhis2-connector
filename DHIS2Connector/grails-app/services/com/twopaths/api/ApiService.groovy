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

package com.twopaths.api

import com.twopaths.dhis2.api.ApiActionType
import com.twopaths.dhis2.api.ApiVersion
import com.twopaths.dhis2.api.Result
import grails.transaction.Transactional
import groovy.json.JsonBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus

/**
 * The ApiService does the actual POSTs/PUTs/GETs/DELETEs to the DHIS 2 API.
 */
@Transactional
class ApiService {

    def apiResultParserFactoryService

    def server
    def context

    // This value can be set globally for the app
    ApiVersion globalApiVersion = ApiVersion.DHIS2_VERSION_225

    final def API_PATH = "/api"

    /**
     * Sets the global default ApiVersion to be used if none is specified
     *
     * @param apiVersion ApiVersion to set as global default
     */
    void setGlobalApiVersion(ApiVersion apiVersion) {
        // do not allow it to be set to null
        if (apiVersion) {
            globalApiVersion = apiVersion
            log.debug("Set global API Version to ${apiVersion}")
        } else {
            log.warn("Unable to set global API Version to ${apiVersion}. Leaving as '${globalApiVersion}'")
        }
    }

    /**
     * POST
     *
     * @param auth DHIS 2 Credentials
     * @param subPath sub path of the API to use for this post
     * @param body The body to post
     * @param query The map of query parameters for this post
     * @param contentType content type to use for this post
     * @param apiVersion ApiVersion to use for this post
     * @return parsed Result object
     */
    Result post(def auth, def subPath, def body=[], def query=[:], def contentType = ContentType.JSON,
                ApiVersion apiVersion = null) {

        // consumer may have explicitly passed in null
        if (!apiVersion) {
            apiVersion = globalApiVersion
        }

        def path = API_PATH + apiVersion.apiVersionSubPath + subPath
        log.debug "post, path: " + path + ", body: " + new JsonBuilder(body).toString() + ", query: " + query

        RESTClient http = getRestClient(auth)

        def response

        // Failure handler...get the response anyway
        http.handler.failure = { resp, json ->
            log.error "Unexpected failure: ${resp.statusLine}, resp: ${resp}, json: ${json}"
            resp.setData(json)
            return resp
        }

        // POST the request and get the response
        try {
            response = http.post(
                    path: context + path,
                    body: new JsonBuilder(body).toString(),
                    query: query,
                    contentType: contentType,
                    requestContentType: ContentType.JSON)
        } catch (Exception e) {
            log.error "Exception: " + e
        }

        if (response?.status in [HttpStatus.SC_OK]) {
            log.debug "put, OK response?.status: ${response?.status}"
        }

        if (response?.status in [HttpStatus.SC_CONFLICT]) {
            log.debug "put, conflict response?.status: ${response?.status}"
        }

        return apiResultParserFactoryService.getParser(apiVersion)
                .parse(ApiActionType.Import, response?.data, response?.status, body)
    }

    /**
     * DELETE
     *
     * @param auth DHIS 2 Credentials
     * @param subPath sub path of the API to use for this delete
     * @param query The map of query parameters for this delete
     * @param contentType content type to use for this delete
     * @param apiVersion ApiVersion to use for this delete
     * @return parsed Result object
     */
    Result delete(def auth, def subPath, def query=[:], def contentType = ContentType.JSON,
                ApiVersion apiVersion = null) {

        // consumer may have explicitly passed in null
        if (!apiVersion) {
            apiVersion = globalApiVersion
        }

        def path = API_PATH + apiVersion.apiVersionSubPath + subPath

        path = context + path

        log.debug "delete, path: " + path + ", query: " + query

        RESTClient http = getRestClient(auth)

        def response

        // Failure handler...get the response anyway
        http.handler.failure = { resp, json ->
            log.error "Unexpected failure: ${resp.statusLine}, resp: ${resp}, json: ${json}"
            resp.setData(json)
            return resp
        }

        // DELETE the request and get the response
        try {
            response = http.delete(
                    path: path,
                    query: query,
                    contentType: contentType,
                    requestContentType: ContentType.JSON)
        } catch (Exception e) {
            log.error "Exception: " + e
            if (!response) {
                response = [
                        status : HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        data : [
                                status : "ERROR",
                                message : e.message
                        ]
                ]
            }
        }

        if (response?.status in [HttpStatus.SC_OK]) {
            log.debug "put, OK response?.status: ${response?.status}"
        }

        if (response?.status in [HttpStatus.SC_CONFLICT]) {
            log.debug "delete, conflict response?.status: ${response?.status}"
        }

        return apiResultParserFactoryService.getParser(apiVersion)
                .parse(ApiActionType.Delete, response?.data, response?.status)
    }

    /**
     * PUT
     *
     * @param auth DHIS 2 Credentials
     * @param subPath sub path of the API to use for this PUT
     * @param body The body to PUT
     * @param id Id of the object to be updated
     * @param query The map of query parameters for this PUT
     * @param contentType content type to use for this PUT
     * @param apiVersion ApiVersion to use for this PUT
     * @return parsed Result object
     */
    Result put(def auth, def subPath, def body, def id, def query=[:],
               def contentType = ContentType.JSON, ApiVersion apiVersion = null) {


        // consumer may have explicitly passed in null
        if (!apiVersion) {
            apiVersion = globalApiVersion
        }

        def path = API_PATH + apiVersion.apiVersionSubPath + subPath

        path = context + path + "/" + id
        body.remove("user");

        log.debug "put, path: " + path
        log.debug "put, body: " + new JsonBuilder(body).toString()
        log.debug "put, id: " + id
        log.debug "put, query: " + query

        RESTClient http = getRestClient(auth)

        def response

        // Failure handler....get the response from the json
        http.handler.failure = { resp, json ->
            log.error "Unexpected failure: ${resp.statusLine}, resp: ${resp}, json: ${json}"
            resp.setData(json)
            return resp
        }

        // PUT the request and get the response
        try {
            response = http.put(
                    path: path,
                    body: new JsonBuilder(body).toString(),
                    query: query,
                    contentType: contentType,
                    requestContentType: ContentType.JSON)
        } catch (Exception e) {
            log.error "Exception: " + e
        }
        log.debug "put, response?.status: " + response?.status

        if (response?.status in [HttpStatus.SC_OK, HttpStatus.SC_CREATED, HttpStatus.SC_ACCEPTED]) {
            log.debug "put, OK response?.status: ${response?.status}"
        }

        if (response?.status in [HttpStatus.SC_CONFLICT]) {
            log.debug "put, conflict response?.status: ${response?.status}"
        }

        return apiResultParserFactoryService.getParser(apiVersion)
                .parse(ApiActionType.Update, response?.data, response?.status, body)
    }

    /**
     * GET
     *
     * @param auth DHIS 2 Credentials
     * @param subPath sub path of the API to use for this GET
     * @param query The map of query parameters for this GET
     * @param queryString Query string of parameters to append to the GET URL
     * @param apiVersion ApiVersion to use for this GET
     * @return parsed Result object
     */
    Result get(def auth, def subPath, def query=[:], def queryString=null,
               ApiVersion apiVersion = null) {

        // consumer may have explicitly passed in null
        if (!apiVersion) {
            apiVersion = globalApiVersion
        }

        def path = API_PATH + apiVersion.apiVersionSubPath + subPath

        query << [preheatCache:false]

        if (query.paging) {
            query << [paging: query.paging]
        } else {
            query << [paging: false]
        }
        log.debug "get, path: " + path + ", query: " + query

        RESTClient http = getRestClient(auth)

        http.handler.failure = { resp ->
            log.debug "Error: " + resp.statusLine.statusCode
        }

        http.handler."404" = { resp ->
            log.debug "404: " + resp
        }

        def response
        try {
            response = http.get(path: context + path, contentType: ContentType.JSON, query: query, queryString: queryString)
        } catch (Exception e) {
            log.error "Exception: " + e
        }
        log.debug "response?.status: " + response?.status

        return apiResultParserFactoryService.getParser(apiVersion)
                .parse(ApiActionType.Get, response?.data, response?.status)
    }


    /**
     * Get REST Client using authentiation from logged in user
     *
     * @param auth DHIS 2 Credentials
     * @return RESTClient
     */
    private def getRestClient(auth) {
        def username
        def password

        if (auth) {
            username = auth.username
            password = auth.password
        } else {
            log.error("No authentication details provided, cannot authenticate")
        }

        def http = new RESTClient(server)

        http.headers['Authorization'] = 'Basic ' + "${username}:${password}".getBytes().encodeBase64()

        return http
    }
}
