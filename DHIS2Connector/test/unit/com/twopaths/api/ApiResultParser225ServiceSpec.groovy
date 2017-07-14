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
import com.twopaths.dhis2.api.Result
import grails.test.mixin.TestFor
import org.apache.http.HttpStatus
import spock.lang.Specification

/**
 * NOTE That 2.25 response structure appears to not have changed since 2.24 response, so same set of tests apply.
 */
@TestFor(ApiResultParser225Service)
class ApiResultParser225ServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test 225 metadata parser"() {

        def requestBody = [
                "programTrackedEntityAttributes": [
                        [
                                "id": "Biv0EB9qg4r"
                        ],
                        [
                                "id": "DHggN4sya9M"
                        ],
                        [
                                "id": "BJTMIu9kGB6"
                        ],
                        [
                                "id": "N7S2HZHusnT"
                        ]
                ],
                "programs"                      : [
                        [
                                "id"  : "lgtr6votm3o",
                                "name": "TEST_PROGRAM",

                        ]
                ]
        ]


        def responseData = [

                "status"     : "OK",
                "stats"      : [
                        "total"  : 5,
                        "created": 0,
                        "updated": 5,
                        "deleted": 0,
                        "ignored": 0
                ],
                "typeReports": [
                        [
                                "klass"        : "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                "stats"        : [
                                        "total"  : 4,
                                        "created": 0,
                                        "updated": 4,
                                        "deleted": 0,
                                        "ignored": 0
                                ],
                                "objectReports": [
                                        [
                                                "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                                "index": 0
                                        ],
                                        [
                                                "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                                "index": 1
                                        ],
                                        [
                                                "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                                "index": 2
                                        ],
                                        [
                                                "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                                "index": 3
                                        ]
                                ]
                        ],
                        [
                                "klass"        : "org.hisp.dhis.program.Program",
                                "stats"        : [
                                        "total"  : 1,
                                        "created": 0,
                                        "updated": 1,
                                        "deleted": 0,
                                        "ignored": 0
                                ],
                                "objectReports": [
                                        [
                                                "klass": "org.hisp.dhis.program.Program",
                                                "index": 0
                                        ]
                                ]
                        ]
                ]

        ]

        def responseStatus = HttpStatus.SC_OK
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.updated == 5

        result.succeeded == 5
        result.conflicts != null
        result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("success") == true
        }

        def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
        programTrackedEntityAttributeSummary != null
        programTrackedEntityAttributeSummary.importCount != null
        programTrackedEntityAttributeSummary.importCount.imported == 0
        programTrackedEntityAttributeSummary.importCount.updated == 4
        programTrackedEntityAttributeSummary.succeeded == 4

        def programSummary = result.importTypeSummaries.get("Program")
        programSummary != null
        programSummary.importCount != null
        programSummary.importCount.imported == 0
        programSummary.importCount.updated == 1
        programSummary.succeeded == 1

    }

    void "test 225 metadata parser with errors"() {

        def requestBody = [
                "programs"                      : [
                        [
                                "id"  : "lgtr6votm3o",
                                "name": "TEST_PROGRAM",
                                "shortName" : "ProgramNameProgramNameProgramNameProgramNameProgramName"

                        ]
                ]
        ]
        // When there is an ERROR, there are no stats for ProgramTrackedEntityAttribute. The entire block is rolled back.
        // there is also no associated "objectReports"
        // have asked DHIS 2 dev list but they have not yet responded.
        def responseData = [
            "status": "ERROR",
            "typeReports": [
                    [
                        "klass": "org.hisp.dhis.program.Program",
                        "stats": [
                            "created": 0,
                            "updated": 0,
                            "deleted": 0,
                            "ignored": 1,
                            "total": 1
                        ],
                        "objectReports": [
                            [
                                "klass": "org.hisp.dhis.program.Program",
                                "index": 0,
                                "uid": "e1Pj90sxBJg",
                                "errorReports": [
                                    [
                                        "message": "Maximum length of property `shortName`is 50, but given length was 64.",
                                        "mainKlass": "org.hisp.dhis.program.Program",
                                        "errorKlass": "java.lang.String",
                                        "errorCode": "E4001"
                                    ]
                                ]
                            ]
                        ]
                    ]
            ],
            "stats": [
                "created": 0,
                "updated": 0,
                "deleted": 0,
                "ignored": 1,
                "total": 1
            ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0
        result.conflicts != null
        result.conflicts?.size() == 1

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 1


        def programSummary = result.importTypeSummaries.get("Program")
        programSummary != null
        programSummary.importCount != null
        programSummary.importCount.imported == 0
        programSummary.importCount.ignored == 1
        programSummary.succeeded == 0
        programSummary.conflicts != null
        programSummary.conflicts.size() == 1

        programSummary.conflicts[0].object == "Program: lgtr6votm3o: TEST_PROGRAM"

    }

    void "Test 225 Parser for Metadata Response for OptionSets and Options" () {
        def requestBody = [
                "options":[
                        [
                                "code":"1",
                                "name":"balaka",
                                "id":"h4ewTptEoiV"
                        ],
                        [
                                "code":"2",
                                "name":"blantyre",
                                "id":"eDcf4T2vgO2"
                        ],
                        [
                                "code":"3",
                                "name":"chikwawa",
                                "id":"f2XihvfocSK"
                        ]
                ],
                "optionSets":[
                        [
                                "access":[
                                        "delete":true,
                                        "externalize":false,
                                        "manage":true,
                                        "read":true,
                                        "update":true,
                                        "write":true
                                ],
                                "attributeValues":[

                                ],
                                "code":"R1 - org_unit",
                                "created":"2017-05-01T11:59:47.392",
                                "displayName":"R1 - org_unit",
                                "externalAccess":false,
                                "href":"http://localhost:8090/dhis/api/25/optionSets/pLNS5x9hzpS",
                                "id":"pLNS5x9hzpS",
                                "lastUpdated":"2017-05-01T11:59:47.392",
                                "name":"R1 - org_unit",
                                "options":[
                                        [
                                                "code":"1",
                                                "name":"balaka",
                                                "id":"h4ewTptEoiV"
                                        ],
                                        [
                                                "code":"2",
                                                "name":"blantyre",
                                                "id":"eDcf4T2vgO2"
                                        ],
                                        [
                                                "code":"3",
                                                "name":"chikwawa",
                                                "id":"f2XihvfocSK"
                                        ]
                                ],
                                "publicAccess":"rw------",
                                "translations":[

                                ],
                                "user":[
                                        "id":"czgaVFeMt4N"
                                ],
                                "userGroupAccesses":[
                                ],
                                "valueType":"INTEGER",
                                "version":0
                        ]
                ]
        ]

        def responseData = [
                "stats":[
                        "created":0,
                        "deleted":0,
                        "ignored":0,
                        "total":4,
                        "updated":4
                ],
                "status":"OK",
                "typeReports":[
                        [
                                "klass":"org.hisp.dhis.option.Option",
                                "stats":[
                                        "created":0,
                                        "deleted":0,
                                        "ignored":0,
                                        "total":3,
                                        "updated":3
                                ]
                        ],
                        [
                                "klass":"org.hisp.dhis.option.OptionSet",
                                "stats":[
                                        "created":0,
                                        "deleted":0,
                                        "ignored":0,
                                        "total":1,
                                        "updated":1
                                ]
                        ]
                ]
        ]

        def responseStatus = HttpStatus.SC_OK
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.created == 0
        result.importCount.updated == 4

        result.succeeded == 4
        result.conflicts != null
        result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("success") == true
        }

        def optionSummary = result.importTypeSummaries.get("Option")
        optionSummary != null
        optionSummary.importCount != null
        optionSummary.importCount.imported == 0
        optionSummary.importCount.created == 0
        optionSummary.importCount.updated == 3
        optionSummary.succeeded == 3

        def optionSetSummary = result.importTypeSummaries.get("OptionSet")
        optionSetSummary != null
        optionSetSummary.importCount != null
        optionSetSummary.importCount.imported == 0
        optionSetSummary.importCount.created == 0
        optionSetSummary.importCount.updated == 1
        optionSetSummary.succeeded == 1
    }

    void "Test 225 Parser for Metadata Response for Options With Errors" () {
        def requestBody = [
                "options":[
                        [
                                "code":"1",
                                "name":"rural",
                                "id":"AlV2xL1t90p"
                        ],
                        [
                                "code":"2",
                                "name":"reallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongyreallylongreallyreallylong",
                                "id":"YEQZNHY42EN"
                        ]
                ],
                "optionSets":[
                        [
                                "access": [
                                        "delete":true,
                                        "externalize":false,
                                        "manage":true,
                                        "read":true,
                                        "update":true,
                                        "write":true
                                ],
                                "attributeValues":[
                                ],
                                "code":"R1 - v025",
                                "created":"2017-05-01T11:59:52.660",
                                "displayName":"R1 - v025",
                                "externalAccess":false,
                                "href":"http://localhost:8090/dhis/api/25/optionSets/sOffAJQlWVf",
                                "id":"sOffAJQlWVf",
                                "lastUpdated":"2017-05-01T11:59:52.660",
                                "name":"R1 - v025",
                                "options":[
                                        [
                                                "code":"1",
                                                "name":"rural",
                                                "id":"AlV2xL1t90p"
                                        ],
                                        [
                                                "code":"2",
                                                "name":"reallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongyreallylongreallyreallylong",
                                                "id":"YEQZNHY42EN"
                                        ]
                                ],
                                "publicAccess":"rw------",
                                "translations":[

                                ],
                                "user":[
                                        "id":"czgaVFeMt4N"
                                ],
                                "userGroupAccesses":[

                                ],
                                "valueType":"INTEGER",
                                "version":0
                        ]
                ]
        ]


        def responseData = [
                "stats": [
                        "created":0,
                        "deleted":0,
                        "ignored":1,
                        "total":3,
                        "updated":2
                ],
                "status":"ERROR",
                "typeReports":[
                        [
                                "klass":"org.hisp.dhis.option.Option",
                                "objectReports":[
                                        [
                                                "errorReports":[
                                                        [
                                                                "errorCode":"E4001",
                                                                "errorKlass":"java.lang.String",
                                                                "mainKlass":"org.hisp.dhis.option.Option",
                                                                "message":"Maximum length of property `name`is 230, but given length was 267."
                                                        ]
                                                ],
                                                "index":1,
                                                "klass":"org.hisp.dhis.option.Option",
                                                "uid":"YEQZNHY42EN"
                                        ]
                                ],
                                "stats":[
                                        "created":0,
                                        "deleted":0,
                                        "ignored":1,
                                        "total":2,
                                        "updated":1
                                ]
                        ],
                        [
                                "klass":"org.hisp.dhis.option.OptionSet",
                                "stats":[
                                        "created":0,
                                        "deleted":0,
                                        "ignored":0,
                                        "total":1,
                                        "updated":1
                                ]
                        ]
                ]
        ]

        def responseStatus = HttpStatus.SC_OK
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.created == 0
        result.importCount.updated == 2

        result.succeeded == 2
        result.conflicts != null
        result.conflicts?.size() == 1

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2

        def optionSummary = result.importTypeSummaries.get("Option")
        def optionSetSummary = result.importTypeSummaries.get("OptionSet")

        optionSummary != null
        optionSummary.get("success") == false
        optionSetSummary != null
        optionSetSummary.get("success") == true

        optionSummary != null
        optionSummary.importCount != null
        optionSummary.importCount.imported == 0
        optionSummary.importCount.ignored == 1
        optionSummary.importCount.created == 0
        optionSummary.importCount.updated == 1
        optionSummary.succeeded == 1

        optionSetSummary != null
        optionSetSummary.importCount != null
        optionSetSummary.importCount.imported == 0
        optionSetSummary.importCount.ignored == 0
        optionSetSummary.importCount.created == 0
        optionSetSummary.importCount.updated == 1
        optionSetSummary.succeeded == 1
    }

    void "Test 225 Parser for Program Post Results"() {
        def requestBody = [
                "programType"        : "WITH_REGISTRATION",
                "name"               : "Program Name",
                "shortName"          : "ProgramName",
                "displayName"        : "Program Name",
                "enrollmentDateLabel": "Survey Date",
                "incidentDateLabel"  : "Survey Date",
                "trackedEntity"      : [
                        "id": "KFZYxD9k1TF"
                ],
                "categoryCombo"      : [
                        "id": "RYnzt2JFGSf"
                ],
                "organisationUnits"  : [
                        [
                                "id": "qubeTudDNSC"
                        ],
                        [
                                "id": "FEGZA6BEAjj"
                        ],
                        [
                                "id": "GeGgihdOJ2p"
                        ]
                ]
        ]

        def responseData = [
                "httpStatus"    : "Created",
                "httpStatusCode": 201,
                "response"      : [
                        "klass"       : "org.hisp.dhis.program.Program",
                        "responseType": "ObjectReport",
                        "uid"         : "O5v2HycDBpT"
                ],
                "status"        : "OK"
        ]

        def responseStatus = 201
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.lastImported == "O5v2HycDBpT"

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 1
        result.importCount.updated == 0

        result.succeeded == 1
        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_CREATED

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for Program Post Results With Error"() {

        def requestBody = [
                "programType"        : "WITH_REGISTRATION",
                "name"               : "Program Name",
                "shortName"          : "ProgramNameProgramNameProgramNameProgramNameProgramName",
                "displayName"        : "Program Name",
                "enrollmentDateLabel": "Survey Date",
                "incidentDateLabel"  : "Survey Date",
                "trackedEntity"      : [
                        "id": "KFZYxD9k1TF"
                ],
                "categoryCombo"      : [
                        "id": "RYnzt2JFGSf"
                ],
                "organisationUnits"  : [
                        [
                                "id": "qubeTudDNSC"
                        ],
                        [
                                "id": "FEGZA6BEAjj"
                        ],
                        [
                                "id": "GeGgihdOJ2p"
                        ]
                ]
        ]

        def responseData = [

                "httpStatus": "Conflict",
                "httpStatusCode": 409,
                "status": "WARNING",
                "message": "One more more errors occurred, please see full details in import report.",
                "response": [
                    "responseType": "ObjectReport",
                    "errorReports": [
                            [
                                "message": "Maximum length of property `shortName`is 50, but given length was 55.",
                                "mainKlass": "org.hisp.dhis.program.Program",
                                "errorKlass": "java.lang.String",
                                "errorCode": "E4001"
                            ]
                    ],
                    "uid": "OzJ8zoXekEX",
                    "klass": "org.hisp.dhis.program.Program"
                ]
        ]

        def responseStatus = 409
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0
        result.conflicts != null
        result.conflicts.size() == 1

        result.status == HttpStatus.SC_CONFLICT

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }


    void "Test 225 Parser for TrackedEntityInstance Create" () {

        def requestBody = [
            "trackedEntity":"KFZYxD9k1TF",
            "orgUnit":"MHcRPWjeguM",
            "attributes":[
                    [
                        "attribute":"H40lWp4FKDK",
                        "value":"2010"
                    ],
                    [
                        "attribute":"qnKpq6XuFAw",
                        "value":"1327"
                    ],
                    [
                        "attribute":"f6yuK8vY5Rs",
                        "value":"3"
                    ]
            ]
        ]

        def responseData = [

            "httpStatus":"OK",
            "httpStatusCode":200,
            "message":"Import was successful.",
            "response":[
                "enrollments":[
                    "deleted":0,
                    "ignored":0,
                    "imported":0,
                    "responseType":"ImportSummaries",
                    "updated":0
                ],
                "importCount":[
                    "deleted":0,
                    "ignored":0,
                    "imported":1,
                    "updated":0
                ],
                "reference":"UCkdDFLIEZC",
                "responseType":"ImportSummary",
                "status":"SUCCESS"
            ],
            "status":"OK"
        ]


        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 1
        result.importCount.created == 1
        result.importCount.updated == 0

        result.succeeded == 1

        result.lastImported == "UCkdDFLIEZC"

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }


    void "Test 225 Parser for TrackedEntityInstance Update" () {
        def requestBody = [

                    "trackedEntity":"KFZYxD9k1TF",
                    "orgUnit":"MHcRPWjeguM",
                    "attributes":[
                        [
                            "attribute":"iflq7Qogt0Y",
                            "value":"2010"
                        ],
                        [
                            "attribute":"d2eH3J9UK9Y",
                            "value":"1327"
                        ]
                ]
        ]

        def responseData =
        [
            "httpStatus":"OK",
            "httpStatusCode":200,
            "message":"Import was successful.",
            "response":[
                "enrollments":[
                    "deleted":0,
                    "ignored":0,
                    "imported":0,
                    "responseType":"ImportSummaries",
                    "updated":0
                ],
                "importCount": [
                    "deleted":0,
                    "ignored":0,
                    "imported":0,
                    "updated":1
                ],
                "reference":"vJs6Ckb9g4X",
                "responseType":"ImportSummary",
                "status":"SUCCESS"
            ],
            "status":"OK"
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Update, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.updated == 1

        result.succeeded == 1
        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for Enrollment Create" () {

        def requestBody = [
                "trackedEntityInstance":"TcrPMk5pDYf",
                "program":"TZQIaZDLHOS",
                "orgUnit":"MHcRPWjeguM",
                "enrollmentDate":"2010-07-01",
                "incidentDate":"2010-07-01"
        ]

        def responseData = [
            "httpStatus":"OK",
            "httpStatusCode":200,
            "message":"Import was successful.",
            "response":[
                "deleted":0,
                "ignored":0,
                "importSummaries":[
                    [
                        "importCount":[
                            "deleted":0,
                            "ignored":0,
                            "imported":1,
                            "updated":0
                        ],
                        "reference":"W5qw1cdGHqm",
                        "responseType":"ImportSummary",
                        "status":"SUCCESS"
                    ]
                ],
                "imported":1,
                "responseType":"ImportSummaries",
                "updated":0
             ],
            "status":"OK"
        ]
        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 1
        result.importCount.created == 1
        result.importCount.updated == 0

        result.succeeded == 1

        result.lastImported == "W5qw1cdGHqm"

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for Data Element Update" () {

        def requestBody = [
                "access":[
                        "delete":true,
                        "externalize":false,
                        "manage":true,
                        "read":true,
                        "update":true,
                        "write":true
                ],
                "aggregationLevels":[

                ],
                "aggregationType":"SUM",
                "attributeValues":[

                ],
                "categoryCombo":[
                        "name":"default"
                ],
                "created":"2016-10-28T16:24:31.721",
                "dataElementGroups":[

                ],
                "dataSets":[
                        [
                                "id":"zwTYtYZnJOL"
                        ]
                ],
                "dimensionItem":"S2tfhlbniSL",
                "dimensionItemType":"DATA_ELEMENT",
                "displayFormName":"Oct28DS - Census Date",
                "displayName":"Oct28DS - Census Date",
                "displayShortName":"Oct28DS - Census Date",
                "domainType":"AGGREGATE",
                "externalAccess":false,
                "href":"http://localhost:8090/dhis/api/24/dataElements/S2tfhlbniSL",
                "id":"S2tfhlbniSL",
                "lastUpdated":"2016-10-28T16:38:20.078",
                "name":"Oct28DS - Census Date",
                "optionSetValue":false,
                "shortName":"Oct28DS - Census Date",
                "translations":[

                ],
                "userGroupAccesses":[

                ],
                "valueType":"DATE",
                "zeroIsSignificant":true
        ]

        def responseData =  [
            "httpStatus":"OK",
            "httpStatusCode":200,
            "response":[
                "klass":"org.hisp.dhis.dataelement.DataElement",
                "responseType":"ObjectReport",
                "uid":"S2tfhlbniSL"
            ],
            "status":"OK"
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.created == 0
        result.importCount.updated == 1

        result.succeeded == 1

        result.lastImported == "S2tfhlbniSL"

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for Data Value Update" () {

        def requestBody = [
                "completeDate":"2015-01-01",
                "dataSet":"zwTYtYZnJOL",
                "period":"2015",
                "dataValues":[
                        [
                                "dataElement":"NUlB2udYdZW",
                                "value":"100.4"
                        ]
                ],
                "orgUnit":"GeGgihdOJ2p"
        ]

        def responseData =  [
            "dataSetComplete":"2015-01-01",
            "description":"Import process completed successfully",
            "importCount":[
                "deleted":0,
                "ignored":0,
                "imported":0,
                "updated":1
            ],
            "responseType":"ImportSummary",
            "status":"SUCCESS"
        ]

        def responseStatus = 200
        // even though this is an update, it is using Import / post
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.created == 0
        result.importCount.updated == 1

        result.succeeded == 1

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }
    void "Test 225 Parser for 204 No Content Response" () {

        def requestBody = [
                "any data should work here"
        ]

        def responseData =  null

        def responseStatus = 204
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_NO_CONTENT

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for Analytics Response" () {

        def requestBody = []

        // Analytics post is an anomoly where there are no import counts returned, which the generic parser would
        // expect for a post.
        def responseData =  [
                "httpStatus": "OK",
                "httpStatusCode": 200,
                "status": "OK",
                "message": "Initiated analytics table update"
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null

        // This should really be true, but generic parser wouldn't know to look for specific message.
        // handle this in ResourceTableService instead manually, checking for message
        result.success == false

        result.message == "Initiated analytics table update"

        result.errors?.size() == 0

        result.conflicts == null || result.conflicts.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for DataSet Create"() {
        def requestBody = [
                "name":"DataSet Name",
                "shortName":"DataSet Name",
                "periodType":"Yearly",
                "openFuturePeriods":"0",
                "categoryCombo":[
                        "id":"RYnzt2JFGSf"
                ],
                "organisationUnits":[
                        [
                                "id":"qubeTudDNSC"
                        ],
                        [
                                "id":"FEGZA6BEAjj"
                        ],
                        [
                                "id":"GeGgihdOJ2p"
                        ]
                ]
        ]

        def responseData = [
                "httpStatus": "Created",
                "httpStatusCode": 201,
                "status": "OK",
                "response": [
                        "responseType": "ObjectReport",
                        "uid": "x8OJEkqiwnH",
                        "klass": "org.hisp.dhis.dataset.DataSet"
                ]
        ]

        def responseStatus = 201
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 0
        result.importCount.imported == 1
        result.importCount.updated == 0

        result.succeeded == 1
        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_CREATED

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for DataSet Delete" () {

        def requestBody = []

        def responseData = [
                "httpStatus": "OK",
                "httpStatusCode": 200,
                "status": "OK",
                "response": [
                    "responseType": "ObjectReport",
                    "uid": "DuOYFiBFz8L",
                    "klass": "org.hisp.dhis.dataset.DataSet"
                ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Delete, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 1
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 1
        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for DataSet Delete with Error" () {

        def requestBody = []

        def responseData = [
                    "httpStatus": "Not Found",
                    "httpStatusCode": 404,
                    "status": "ERROR",
                    "message": "DataSet with id vQ5q4zkHu could not be found."
        ]


        def responseStatus = 404
        Result result = service.parse(ApiActionType.Delete, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0

        result.status == HttpStatus.SC_NOT_FOUND

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0


    }

    void "Test 225 Parser for DataElement Delete" () {

        def requestBody = []

        def responseData = [
                "httpStatus": "OK",
                "httpStatusCode": 200,
                "status": "OK",
                "response": [
                    "responseType": "ObjectReport",
                    "uid": "ElCs3fxnKV5",
                    "klass": "org.hisp.dhis.dataelement.DataElement"
                ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Delete, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null

        result.importCount.deleted == 1
        result.importCount.ignored == 0
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 1
        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }

    void "Test 225 Parser for DataElement Delete with Error" () {

        def requestBody = []

        def responseData = [
                    "httpStatus": "Conflict",
                    "httpStatusCode": 409,
                    "status": "ERROR",
                    "message": "Could not delete due to association with another object: DataValue"
        ]


        def responseStatus = 409
        Result result = service.parse(ApiActionType.Delete, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0

        result.status == HttpStatus.SC_CONFLICT

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0


    }

    void "Test 225 Parser for DataElement Prune" () {

        def requestBody = []

        def responseData = []

        def responseStatus = 204
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount == null

        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_NO_CONTENT

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0

    }


    void "Test 225 Parser for Delete Internal Server Error" () {
        def requestBody = []

        def responseData = [
                "status":500,
                "data": [
                        "status":"ERROR","message":"Cannot set a request body for a DELETE method"]
                ]

        def responseStatus = 500
        Result result = service.parse(ApiActionType.Delete, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount == null

        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_INTERNAL_SERVER_ERROR

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0
    }

    void "Test 225 Parser for Events Delete" () {

        /*
        {"httpStatus":"OK","httpStatusCode":200,"message":"Import was successful.","response":{"deleted":7,"ignored":0,"importSummaries":[{"description":"Deletion of event NEIZ9H2eTIR was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event cTvfASdCZPr was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event f3RThXfOuHh was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event ACF8YGQZ6m5 was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event AQtC5UqwxgB was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event D7yuZI00g1I was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"},{"description":"Deletion of event sh92dlOSAy3 was successful","importCount":{"deleted":1,"ignored":0,"imported":0,"updated":0},"responseType":"ImportSummary","status":"SUCCESS"}],"imported":0,"responseType":"ImportSummaries","status":"SUCCESS","updated":0},"status":"OK"}

         */

        def requestBody = [
                "events": [
                    [
                        "event": "d40kkWJ5AJg"
                    ],
                    [
                        "event": "o09mw1Cj0Uw"
                    ],
                    [
                        "event": "wNeq0dX01vF"
                    ]
                ]
        ]

        def responseData = [
                "httpStatus": "OK",
                "httpStatusCode": 200,
                "status": "OK",
                "message": "Import was successful.",
                "response": [
                    "responseType": "ImportSummaries",
                    "status": "SUCCESS",
                    "imported": 0,
                    "updated": 0,
                    "deleted": 3,
                    "ignored": 0,
                    "importSummaries": [
                            [
                                "responseType": "ImportSummary",
                                "status": "SUCCESS",
                                "description": "Deletion of event d40kkWJ5AJg was successful",
                                "importCount": [
                                    "imported": 0,
                                    "updated": 0,
                                    "ignored": 0,
                                    "deleted": 1
                                ]
                            ],
                            [
                                "responseType": "ImportSummary",
                                "status": "SUCCESS",
                                "description": "Deletion of event o09mw1Cj0Uw was successful",
                                "importCount": [
                                    "imported": 0,
                                    "updated": 0,
                                    "ignored": 0,
                                    "deleted": 1
                                ]
                            ],
                            [
                                "responseType": "ImportSummary",
                                "status": "SUCCESS",
                                "description": "Deletion of event wNeq0dX01vF was successful",
                                "importCount": [
                                    "imported": 0,
                                    "updated": 0,
                                    "ignored": 0,
                                    "deleted": 1
                                ]
                            ]
                    ]
                ]
        ]
        def responseStatus = 200

        // even though this is a delete, it is POSTed and action is "Import"
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null
        result.importCount.deleted == 3

        result.succeeded == 3

        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

    }

    void "Test 225 Parser for Events Delete with Errors" () {

        def requestBody = [
                "events": [
                        [
                                "event": "d40kkWJ5AJg"
                        ],
                        [
                                "event": "o09mw1Cj0Uw"
                        ]
                ]
        ]

        def responseData = [
                "httpStatus": "Conflict",
                "httpStatusCode": 409,
                "status": "WARNING",
                "message": "One more conflicts encountered, please check import summary.",
                "response": [
                        "responseType": "ImportSummaries",
                        "status": "SUCCESS",
                        "imported": 0,
                        "updated": 0,
                        "deleted": 0,
                        "ignored": 2,
                        "importSummaries": [
                                [
                                        "responseType": "ImportSummary",
                                        "status": "ERROR",
                                        "description": "ID d40kkWJ5AJg does not point to a valid event: d40kkWJ5AJg",
                                        "importCount": [
                                                "imported": 0,
                                                "updated": 0,
                                                "ignored": 1,
                                                "deleted": 0
                                        ]
                                ],
                                [
                                        "responseType": "ImportSummary",
                                        "status": "ERROR",
                                        "description": "ID NQsmcain6mq does not point to a valid event: NQsmcain6mq",
                                        "importCount": [
                                                "imported": 0,
                                                "updated": 0,
                                                "ignored": 1,
                                                "deleted": 0
                                        ]
                                ]
                        ]
                ]
        ]
        def responseStatus = 409

        // even though this is a delete, it is POSTed and action is "Import"
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 2
        result.importCount != null
        result.importCount.ignored == 2
        result.succeeded == 0

        result.conflicts?.size() == 2
        result.errors?.size() == 2

        result.status == HttpStatus.SC_CONFLICT

    }

    void "Test 225 Parser for Tracked Entity Instance Bulk Delete" () {

        def requestBody = [

                "trackedEntityInstances":[
                    [
                        "trackedEntityInstance":"fTPgeT6NFi6"
                    ],
                    [
                        "trackedEntityInstance":"c8g0uzDBv3Q"
                    ],
                    [
                        "trackedEntityInstance":"DPBCUPs7Ls7"
                    ],
                    [
                        "trackedEntityInstance":"hVbnpIk8Ff8"
                    ]
                ]


        ]
        def responseData = [

                "deleted":3,
                "ignored":0,
                "importSummaries":[
                    [
                        "description":"Deletion of tracked entity instance fTPgeT6NFi6 was successful",
                        "importCount": [
                            "deleted":1,
                            "ignored":0,
                            "imported":0,
                            "updated":0
                        ],
                        "responseType":"ImportSummary",
                        "status":"SUCCESS"
                    ],
                    [
                        "description":"Deletion of tracked entity instance c8g0uzDBv3Q was successful",
                        "importCount":[
                            "deleted":1,
                            "ignored":0,
                            "imported":0,
                            "updated":0
                        ],
                        "responseType":"ImportSummary",
                        "status":"SUCCESS"
                    ],
                    [
                        "description":"Deletion of tracked entity instance DPBCUPs7Ls7 was successful",
                        "importCount":[
                            "deleted":1,
                            "ignored":0,
                            "imported":0,
                            "updated":0
                        ],
                        "responseType":"ImportSummary",
                        "status":"SUCCESS"
                    ]
            ],
                "imported":0,
                "responseType":"ImportSummaries",
                "status":"SUCCESS",
                "updated":0
        ]

        def responseStatus = 201

        // Note that the return type is Import for bulk delete (POST with strategy=DELETE)
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == true

        result.errors?.size() == 0
        result.importCount != null
        result.importCount.deleted == 3

        result.succeeded == 3

        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_CREATED

    }
}
