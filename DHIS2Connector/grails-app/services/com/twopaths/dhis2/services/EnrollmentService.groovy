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
 * Service to do Enrollment CRUD with the DHIS 2 API
 */
@Transactional
class EnrollmentService {

    final def PATH = "/enrollments"

    def apiService

    /**
     * Creates an Enrollment via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param enrollment Enrollment object to create
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return The Result of the API creation
     */
    def create(def auth, def enrollment, def query = [:], ApiVersion apiVersion = null) {

        def result = apiService.post(auth, PATH, enrollment, query, ContentType.JSON, apiVersion)

        log.debug "create, result: " + result

        return result
    }

    /**
     * Updates an Enrollment via the DHIS 2 API
     *
     * @param auth DHIS 2 Credentials
     * @param enrollment The Enrollment object to update
     * @param query Map of query parameters
     * @param apiStrategy The ApiStrategy to use for the update
     * @param apiVersion ApiVersion to use
     * @return the Result of the API update
     */
    def update(def auth, def enrollment, def query = [:], ApiStrategy apiStrategy = ApiStrategy.CREATE_AND_UPDATE,
               ApiVersion apiVersion = null) {

        query.put("strategy", ApiStrategy.CREATE_AND_UPDATE.value())

        def result = apiService.put(auth, PATH, enrollment, enrollment.id, query, ContentType.JSON, apiVersion)

        log.debug "update, result: " + result

        return result
    }

    /**
     * Retrieves the enrollment with the specified code
     *
     * @param auth DHIS 2 Credentials
     * @param code The code to find the enrollment for
     * @param fields array of fields to return in the enrollment
     * @param apiVersion ApiVersion to use
     * @return the enrollment found
     */
    def get(def auth, def code, ArrayList<String> fields = [],
            ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def enrollment = apiService.get(auth, "${PATH}?code=${code}", queryParams, null, apiVersion)?.data

        log.debug "get, enrollment: " + enrollment

        return enrollment
    }

    /**
     * Finds enrollments based on the query parameters supplied
     *
     * @param auth DHIS 2 Credentials
     * @param query Map of query parameters
     * @param apiVersion ApiVersion to use
     * @return found enrollments
     */
    def findByQuery(def auth, def query = [:], ApiVersion apiVersion = null) {

        def enrollments = apiService.get(auth,  PATH, query, null, apiVersion)?.data

        log.debug "enrollments: " + enrollments

        return enrollments
    }

    /**
     * Finds enrollments with the specified org unit and tracked entity instance
     *
     * @param auth DHIS 2 Credentials
     * @param orgUnit Org unit to find enrollments for
     * @param trackedEntityInstanceId Tracked Entity Instance to find enrollments for
     * @param apiVersion ApiVersion to use
     * @return found enrollments
     */
    def findByOrgUnitAndTrackedEntityInstance(def auth, def orgUnit, def trackedEntityInstanceId,
                                              ApiVersion apiVersion = null) {

        def queryParams = [ou: orgUnit, trackedEntityInstance: trackedEntityInstanceId, fields: ":all"]

        def enrollments = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        log.debug "enrollments: " + enrollments

        if (enrollments?.enrollments?.size() == 1) {
            return enrollments.enrollments[0]
        } else {
            log.error "No enrollments found for trackedEntityInstanceId: " + trackedEntityInstanceId
            return null
        }
    }

    /**
     * Creates a lookup map of enrollments by the enrollment's code
     *
     * @param auth DHIS 2 Credentials
     * @param fields fields to return in the enrollment object graph
     * @param apiVersion ApiVersion to use
     * @return a lookup map of enrollments by code
     */
    def getLookup(def auth, ArrayList<String> fields = [":all"],
                  ApiVersion apiVersion = null) {

        def queryParams = [:]

        if (fields?.size() > 0) {
            queryParams.put("fields", fields.join(','))
        }

        def lookup = [:]

        def allEnrollments = []

        def enrollments = apiService.get(auth, "${PATH}", queryParams, null, apiVersion)?.data

        allEnrollments.addAll(enrollments.enrollments)

        // Create the lookup from the tracked entity attributes
        allEnrollments.each { enrollment ->
            lookup << [("${enrollment.code}".toString()): enrollment]
        }

        return lookup
    }
}
