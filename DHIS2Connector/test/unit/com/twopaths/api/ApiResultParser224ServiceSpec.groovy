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
 * NOTE That 2.24 response structure appears to not have changed since 2.23 response, so same set of tests apply.
 */
@TestFor(ApiResultParser224Service)
class ApiResultParser224ServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test 224 metadata parser"() {

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

    void "test 224 metadata parser with errors"() {

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
        // When there is an ERROR, there are no stats for ProgramTrackedEntityAttribute. The entire block is rolled back.
        // there is also no associated "objectReports"
        // have asked DHIS 2 dev list but they have not yet responded.
        def responseData = [
                "status"     : "ERROR",
                "stats"      : [
                        "total"  : 1,
                        "created": 0,
                        "updated": 0,
                        "deleted": 0,
                        "ignored": 1
                ],
                "typeReports": [
                        [
                                "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                                "stats": [
                                        "total"  : 0,
                                        "created": 0,
                                        "updated": 0,
                                        "deleted": 0,
                                        "ignored": 0
                                ]
                        ],
                        [
                                "klass"        : "org.hisp.dhis.program.Program",
                                "stats"        : [
                                        "total"  : 1,
                                        "created": 0,
                                        "updated": 0,
                                        "deleted": 0,
                                        "ignored": 1
                                ],
                                "objectReports": [
                                        [
                                                "klass"       : "org.hisp.dhis.program.Program",
                                                "index"       : 0,
                                                "errorReports": [
                                                        [
                                                                "message"   : "Maximum length of property \"shortName\"is 50, but given length was 150.",
                                                                "mainKlass" : "org.hisp.dhis.program.Program",
                                                                "errorKlass": "java.lang.String",
                                                                "errorCode" : "E4001"
                                                        ]
                                                ]
                                        ]
                                ]
                        ]
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
        result.importTypeSummaries.size() == 2

        def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
        programTrackedEntityAttributeSummary != null
        programTrackedEntityAttributeSummary.importCount != null
        programTrackedEntityAttributeSummary.importCount.imported == 0
        programTrackedEntityAttributeSummary.importCount.updated == 0
        programTrackedEntityAttributeSummary.importCount.ignored == 0
        programTrackedEntityAttributeSummary.succeeded == 0

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


    void "Test 224 Parser for Metadata Response for OptionSets and Options" () {
        def requestBody = [
                    "options":[
                        [
                            "id":"Ww1y30FgMnc",
                            "name":"o1",
                            "code":"o1"
                        ],
                        [
                            "id":"JiFxexQK66o",
                            "name":"o2",
                            "code":"o2"
                        ],
                        [
                            "id":"Y8ys6Z0kAKA",
                            "name":"o3",
                            "code":"o3"
                        ]
                ],
                    "optionSets":[
                        [
                            "name":"testOptionSet",
                            "valueType":"TEXT",
                            "options":[
                                [
                                    "id":"Ww1y30FgMnc"
                                ],
                                [
                                    "id":"JiFxexQK66o"
                                ],
                                [
                                    "id":"Y8ys6Z0kAKA"
                                ]
                        ]
                    ]
                ]

        ]


        def responseData = [
                    "status":"OK",
                    "typeReports":[
                        [
                            "klass":"org.hisp.dhis.option.OptionSet",
                            "stats":[
                                "total":1,
                                "created":1,
                                "updated":0,
                                "deleted":0,
                                "ignored":0
                            ]
                        ],
                        [
                            "klass":"org.hisp.dhis.option.Option",
                            "stats":[
                                "total":3,
                                "created":3,
                                "updated":0,
                                "deleted":0,
                                "ignored":0
                            ]
                        ]
                    ],
                    "stats":[
                        "total":4,
                        "created":4,
                        "updated":0,
                        "deleted":0,
                        "ignored":0
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
        result.importCount.imported == 4
        result.importCount.created == 4
        result.importCount.updated == 0

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
        optionSummary.importCount.imported == 3
        optionSummary.importCount.created == 3
        optionSummary.importCount.updated == 0
        optionSummary.succeeded == 3

        def optionSetSummary = result.importTypeSummaries.get("OptionSet")
        optionSetSummary != null
        optionSetSummary.importCount != null
        optionSetSummary.importCount.imported == 1
        optionSetSummary.importCount.created == 1
        optionSetSummary.importCount.updated == 0
        optionSetSummary.succeeded == 1
    }

    void "Test 224 Parser for Metadata Response for Options With Errors" () {
        def requestBody = [
                "options":[
                        [
                                "id":"Ww1y30FgMnc",
                                "name":"o1",
                                "code":"o1"
                        ],
                        [
                                "id":"JiFxexQK66o",
                                "name":"reallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongreallyreallylongyreallylongreallyreallylong",
                                "code":"o2"
                        ],
                        [
                                "id":"Y8ys6Z0kAKA",
                                "name":"o3",
                                "code":"o3"
                        ]
                ]
        ]


        def responseData = [
                "status":"ERROR",
                "typeReports":[
                    [
                        "klass":"org.hisp.dhis.option.Option",
                        "stats": [
                            "total":1,
                            "created":0,
                            "updated":0,
                            "deleted":0,
                            "ignored":1
                        ],
                        "objectReports":[
                            [
                                "klass":"org.hisp.dhis.option.Option",
                                "index":1,
                                "uid":"Lm4t2ArTbb3",
                                "errorReports":[
                                    [
                                        "message":"Maximum length of property `name`is 230, but given length was 267.",
                                        "mainKlass":"org.hisp.dhis.option.Option",
                                        "errorKlass":"java.lang.String",
                                        "errorCode":"E4001"
                                    ]
                                ]
                            ]
                        ]
                    ]
                ],
                "stats": [
                    "total":1,
                    "created":0,
                    "updated":0,
                    "deleted":0,
                    "ignored":1
                ]
        ]

        def responseStatus = HttpStatus.SC_OK
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus, requestBody)

        expect:

        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        // NOTE: this is kind of wrong (should be 3 ignored, not 1), but DHIS 2 in v 2.24 is not returning the correct
        // number of total records, just the number of ones that didn't work, so this test is written to what is
        // actually returned by DHIS 2
        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.created == 0
        result.importCount.updated == 0

        result.succeeded == 0
        result.conflicts != null
        result.conflicts?.size() == 1

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 1
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("success") == false
        }

        def optionSummary = result.importTypeSummaries.get("Option")
        optionSummary != null
        optionSummary.importCount != null
        optionSummary.importCount.imported == 0
        optionSummary.importCount.ignored == 1
        optionSummary.importCount.created == 0
        optionSummary.importCount.updated == 0
        optionSummary.succeeded == 0

    }

    void "Test 224 Parser for Program Post Results"() {
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

    void "Test 224 Parser for Program Post Results With Error"() {

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

                "httpStatus"    : "Conflict",
                "httpStatusCode": 409,
                "message"       : "One more more errors occurred, please see full details in import report.",
                "response"      : [
                        "errorReports": [
                                [
                                        "errorCode": "E5003",
                                        "mainKlass": "org.hisp.dhis.program.Program",
                                        "message"  : "Property `shortName`\u00a0with value `OCT18TEST3` on object OCT18TEST3 [s6VgoaPoIdA] (Program)\u00a0already exists on object XYBcJkKho1A."
                                ]
                        ],
                        "klass"       : "org.hisp.dhis.program.Program",
                        "responseType": "ObjectReport"
                ],
                "status"        : "WARNING"


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


    void "Test 224 Parser for TrackedEntityInstance Create" () {

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


    void "Test 224 Parser for TrackedEntityInstance Update" () {
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

    void "Test 224 Parser for Enrollment Create" () {

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

    void "Test 224 Parser for Data Element Update" () {

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

    void "Test 224 Parser for Data Value Update" () {

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
    void "Test 224 Parser for 204 No Content Response" () {

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

    void "Test 224 Parser for Analytics Response" () {

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

    void "Test 224 Parser for DataSet Create"() {
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


}
