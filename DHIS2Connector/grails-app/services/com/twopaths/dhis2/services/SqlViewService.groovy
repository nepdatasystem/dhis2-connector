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
 * Service to do SqlView CRUD with the DHIS 2 API
 *
 * These are the views that the system is relying on to determine whether datasets /programs have data or not.
 * They need to be created upon instance set-up.
 *
 * nep.sqlview.datasets.with.data.name from nep.properties ("Data Sets With Data"):
 *
 * select distinct ds.uid from datasetmembers dsm inner join dataset ds on dsm.datasetid=ds.datasetid where exists (select 1 from datavalue dv where dv.dataelementid=dsm.dataelementid);
 *
 * nep.sqlview.programs.with.data.name ("Programs With Data")
 *
 * select distinct pr.uid from program pr inner join programinstance pi on pr.programid = pi.programid inner join trackedentityinstance tei on pi.trackedentityinstanceid = tei.trackedentityinstanceid;
 *
 * nep.sqlview.program.stages.with.data.name from nep.properties ("Program Stages With Data"):
 *
 * select distinct ps.uid from programstage ps inner join programstageinstance psi on ps.programstageid = psi.programstageid;
 *
 */
@Transactional
class SqlViewService {

    final def PATH = "/sqlViews"

    def apiService

    /**
     * Finds the SqlView with the specified name
     *
     * @param auth DHIS 2 Credentials
     * @param name The name of the sql view to find
     * @param apiVersion ApiVersion to use
     * @return The sql view found if any
     */
    def findByName (def auth, def name, ApiVersion apiVersion = null) {

        def data = apiService.get(auth, PATH, [filter: "name:eq:${name}"], null, apiVersion)?.data

        log.debug "sql view ${} response: ${data}"

        def sqlView = data?.sqlViews?.size() > 0 ? data.sqlViews[0] : null

        return sqlView
    }

    /**
     * Retrieves the view data for the specified view
     *
     * @param auth DHIS 2 Credentials
     * @param sqlViewId
     * @param apiVersion ApiVersion to use
     * @return The view data for the specified view
     */
    def getViewData (def auth, def sqlViewId, ApiVersion apiVersion = null) {

        if (!sqlViewId) {
            return null
        }

        def data = apiService.get(auth, PATH + "/${sqlViewId}/data", [:], null, apiVersion)?.data

        log.debug "sql view data for id ${sqlViewId} : ${data}"

        return data?.rows
    }

    /**
     * Executes the specified view. This creates the view in the database if it didn't already exist.
     *
     * @param auth DHIS 2 Credentials
     * @param sqlViewId The id of the sql view to execute
     * @param apiVersion ApiVersion to use
     * @return The parsed Result object from the API
     */
    def executeView (def auth, def sqlViewId, ApiVersion apiVersion = null) {

        if (!sqlViewId) {
            return null
        }

        def result = apiService.post(auth, PATH + "/${sqlViewId}/execute", [], [:], ContentType.ANY, apiVersion)?.data

        log.debug "sql view execution for id ${sqlViewId} : ${result}"

        return result

    }
}
