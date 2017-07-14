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
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(ApiResultParserDefaultService)
class ApiResultParserDefaultServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test getSucceededCount"() {
        expect :
        // should ignore "ignored" and "deleted"
        service.getSucceededCount(ApiActionType.Import, ["imported": 1, "updated": 3, "ignored": 1, "deleted": 1]) == 4

        // should ignore "imported", "ignored" and "deleted"
        service.getSucceededCount(ApiActionType.Update, ["imported" : 1, "updated" : 3, "ignored" : 1, "deleted" : 1]) == 3

    }

    void "test default parser"() {

        def responseData = [
            "importCount" : [
                    "deleted" : 0,
                    "ignored" : 0,
                    "imported" : 4,
                    "updated" : 1
                ],
            "importTypeSummaries" : [
                    [
                        "importCount" : [
                                "deleted" : 0,
                                "ignored" : 0,
                                "imported" : 4,
                                "updated" : 0,

                        ],
                        "lastImported" : "HhxD6otLUGb",
                        "responseType" : "ImportTypeSummary",
                        "status" : "SUCCESS",
                        "type" : "ProgramTrackedEntityAttribute"
                    ],
                    [
                        "importCount" : [
                                "deleted" : 0,
                                "ignored" : 0,
                                "imported" : 0,
                                "updated" : 1,

                        ],
                        "lastImported" : "iHsv0pCOcl2",
                        "responseType" : "ImportTypeSummary",
                        "status" : "SUCCESS",
                        "type" : "Program"
                    ]
            ]
        ]
        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus)

        expect:
            result != null
            result.success == true

            result.errors?.size() == 0
            result.importCount != null

            result.importCount.deleted == 0
            result.importCount.ignored == 0
            result.importCount.imported == 4
            result.importCount.updated == 1

            result.succeeded == 5
            result.conflicts != null
            result.conflicts?.size() == 0

            result.status == HttpStatus.SC_OK

            result.importTypeSummaries != null
            result.importTypeSummaries.size() == 2
            result.importTypeSummaries.each { apiObjectName, summary ->
                summary.get("status") == "SUCCESS"
                summary.get("success") == true
            }

            def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
            programTrackedEntityAttributeSummary != null
            programTrackedEntityAttributeSummary.importCount != null
            programTrackedEntityAttributeSummary.importCount.imported == 4
            programTrackedEntityAttributeSummary.importCount.updated == 0
            programTrackedEntityAttributeSummary.succeeded == 4
            programTrackedEntityAttributeSummary.lastImported == "HhxD6otLUGb"

            def programSummary = result.importTypeSummaries.get("Program")
            programSummary != null
            programSummary.importCount != null
            programSummary.importCount.imported == 0
            programSummary.importCount.updated == 1
            programSummary.succeeded == 1
            programSummary.lastImported == "iHsv0pCOcl2"
    }

    void "test importTypeSummaries conflict" () {
        def responseData = [
            "importCount": [
                "imported": 0,
                "updated": 4,
                "ignored": 1,
                "deleted": 0
            ],
            "importTypeSummaries": [
                    [
                        "responseType": "ImportTypeSummary",
                        "status": "SUCCESS",
                        "importCount": [
                            "imported": 0,
                            "updated": 4,
                            "ignored": 0,
                            "deleted": 0
                        ],
                        "type": "ProgramTrackedEntityAttribute",
                        "lastImported": "N7S2HZHusnT"
                    ],
                    [
                        "responseType": "ImportTypeSummary",
                        "status": "SUCCESS",
                        "importCount": [
                            "imported": 0,
                            "updated": 0,
                            "ignored": 1,
                            "deleted": 0
                        ],
                        "type": "Program",
                        "importConflicts": [
                            [
                                "object": "AUG18TEST2",
                                "value": "Validation Violations: [ErrorReport{message=Maximum length of property \"shortName\"is 50, but given length was 150., errorCode=E4001, mainKlass=class org.hisp.dhis.program.Program, errorKlass=class java.lang.String, value=null}]"
                            ]
                    ]
                    ]
            ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus)

        expect:
        result != null
        result.success == false

        result.errors?.size() == 1
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 1
        result.importCount.imported == 0
        result.importCount.updated == 4

        result.succeeded == 4
        result.conflicts != null
        result.conflicts?.size() == 1

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("status") == "SUCCESS"
        }

        def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
        programTrackedEntityAttributeSummary != null
        programTrackedEntityAttributeSummary.importCount != null
        programTrackedEntityAttributeSummary.importCount.imported == 0
        programTrackedEntityAttributeSummary.importCount.updated == 4
        programTrackedEntityAttributeSummary.succeeded == 4
        programTrackedEntityAttributeSummary.lastImported == "N7S2HZHusnT"
        programTrackedEntityAttributeSummary.success == true

        def programSummary = result.importTypeSummaries.get("Program")
        programSummary != null
        programSummary.importCount != null
        programSummary.importCount.imported == 0
        programSummary.importCount.ignored == 1
        programSummary.succeeded == 0
        programSummary.lastImported == null
        programSummary.success == false
        programSummary.conflicts != null
        programSummary.conflicts.size() == 1


    }
    void "test importTypeSummaries multiple conflicts" () {
        def responseData = [
                "importCount": [
                        "imported": 0,
                        "updated": 0,
                        "ignored": 2,
                        "deleted": 0
                ],
                "importTypeSummaries": [
                        [
                                "responseType": "ImportTypeSummary",
                                "status": "SUCCESS",
                                "importCount": [
                                        "imported": 0,
                                        "updated": 0,
                                        "ignored": 1,
                                        "deleted": 0
                                ],
                                "type": "ProgramTrackedEntityAttribute",
                                "importConflicts": [
                                        [
                                                "object": "ProgramTrackedEntityAttribute#5",
                                                "value": "some error message"
                                        ]
                                ]
                        ],
                        [
                                "responseType": "ImportTypeSummary",
                                "status": "SUCCESS",
                                "importCount": [
                                        "imported": 0,
                                        "updated": 0,
                                        "ignored": 1,
                                        "deleted": 0
                                ],
                                "type": "Program",
                                "importConflicts": [
                                        [
                                                "object": "AUG18TEST2",
                                                "value": "Validation Violations: [ErrorReport{message=Maximum length of property \"shortName\"is 50, but given length was 150., errorCode=E4001, mainKlass=class org.hisp.dhis.program.Program, errorKlass=class java.lang.String, value=null}]"
                                        ]
                                ]
                        ]
                ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus)

        expect:
        result != null
        result.success == false

        result.errors?.size() == 2
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 2
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0
        result.conflicts != null
        result.conflicts?.size() == 2

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("status") == "SUCCESS"
            summary.get("success") == false
        }

        def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
        programTrackedEntityAttributeSummary != null
        programTrackedEntityAttributeSummary.importCount != null
        programTrackedEntityAttributeSummary.importCount.imported == 0
        programTrackedEntityAttributeSummary.importCount.ignored == 1
        programTrackedEntityAttributeSummary.succeeded == 0
        programTrackedEntityAttributeSummary.lastImported == null
        programTrackedEntityAttributeSummary.conflicts != null
        programTrackedEntityAttributeSummary.conflicts.size() == 1

        def programSummary = result.importTypeSummaries.get("Program")
        programSummary != null
        programSummary.importCount != null
        programSummary.importCount.imported == 0
        programSummary.importCount.ignored == 1
        programSummary.succeeded == 0
        programSummary.lastImported == null
        programSummary.conflicts != null
        programSummary.conflicts.size() == 1


    }
    void "test importTypeSummaries ERROR status" () {
        def responseData = [
                "importCount": [
                        "imported": 0,
                        "updated": 0,
                        "ignored": 2,
                        "deleted": 0
                ],
                "importTypeSummaries": [
                        [
                                "responseType": "ImportTypeSummary",
                                "status": "ERROR",
                                "importCount": [
                                        "imported": 0,
                                        "updated": 0,
                                        "ignored": 1,
                                        "deleted": 0
                                ],
                                "type": "ProgramTrackedEntityAttribute",
                                "importConflicts": [
                                        [
                                                "object": "ProgramTrackedEntityAttribute#5",
                                                "value": "some error message"
                                        ]
                                ]
                        ],
                        [
                                "responseType": "ImportTypeSummary",
                                "status": "SUCCESS",
                                "importCount": [
                                        "imported": 0,
                                        "updated": 0,
                                        "ignored": 1,
                                        "deleted": 0
                                ],
                                "type": "Program",
                                "importConflicts": [
                                        [
                                                "object": "AUG18TEST2",
                                                "value": "Validation Violations: [ErrorReport{message=Maximum length of property \"shortName\"is 50, but given length was 150., errorCode=E4001, mainKlass=class org.hisp.dhis.program.Program, errorKlass=class java.lang.String, value=null}]"
                                        ]
                                ]
                        ]
                ]
        ]

        def responseStatus = 200
        Result result = service.parse(ApiActionType.Import, responseData, responseStatus)

        expect:
        result != null
        result.success == false

        // extra error added when status is ERROR
        result.errors?.size() == 3
        result.importCount != null

        result.importCount.deleted == 0
        result.importCount.ignored == 2
        result.importCount.imported == 0
        result.importCount.updated == 0

        result.succeeded == 0
        result.conflicts != null
        result.conflicts?.size() == 2

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries != null
        result.importTypeSummaries.size() == 2
        result.importTypeSummaries.each { apiObjectName, summary ->
            summary.get("success") == false
        }

        def programTrackedEntityAttributeSummary = result.importTypeSummaries.get("ProgramTrackedEntityAttribute")
        programTrackedEntityAttributeSummary != null
        programTrackedEntityAttributeSummary.importCount != null
        programTrackedEntityAttributeSummary.importCount.imported == 0
        programTrackedEntityAttributeSummary.importCount.ignored == 1
        programTrackedEntityAttributeSummary.succeeded == 0
        programTrackedEntityAttributeSummary.lastImported == null
        programTrackedEntityAttributeSummary.conflicts != null
        programTrackedEntityAttributeSummary.conflicts.size() == 1
        programTrackedEntityAttributeSummary.status == "ERROR"

        def programSummary = result.importTypeSummaries.get("Program")
        programSummary != null
        programSummary.importCount != null
        programSummary.importCount.imported == 0
        programSummary.importCount.ignored == 1
        programSummary.succeeded == 0
        programSummary.lastImported == null
        programSummary.conflicts != null
        programSummary.conflicts.size() == 1
        programSummary.status == "SUCCESS"


    }

    void "Test Default Parser for Program Post Results" () {
        def requestBody = [
                "programType":"WITH_REGISTRATION",
                "name":"Program Name",
                "shortName":"ProgramName",
                "displayName":"Program Name",
                "enrollmentDateLabel":"Survey Date",
                "incidentDateLabel":"Survey Date",
                "trackedEntity":[
                        "id":"KFZYxD9k1TF"
                ],
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
                "httpStatus":"OK",
                "httpStatusCode":200,
                "message":"Import was successful.",
                "response":[
                    "importCount":[
                        "deleted":0,
                        "ignored":0,
                        "imported":1,
                        "updated":0
                    ],
                    "lastImported":"IjFuceatKHF",
                    "responseType":"ImportTypeSummary",
                    "status":"SUCCESS",
                    "type":"Program"
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
        result.importCount.updated == 0

        result.succeeded == 1
        result.conflicts == null || result.conflicts?.size() == 0

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0


    }
    void "Test Default Parser for Program Post Results With Error"() {

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

                    "httpStatus":"OK",
                    "httpStatusCode":200,
                    "message":"Import was successful.",
                    "response":[
                        "importConflicts":[
                                [
                                    "object":"OCT18TEST3",
                                    "value":"Object already exists."
                                ]
                        ],
                        "importCount":[
                            "deleted":0,
                            "ignored":1,
                            "imported":0,
                            "updated":0
                        ],
                        "responseType":"ImportTypeSummary",
                        "status":"SUCCESS",
                        "type":"Program"
                    ],
                    "status":"OK"

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
        result.conflicts?.size() == 1

        result.status == HttpStatus.SC_OK

        result.importTypeSummaries == null || result.importTypeSummaries.size() == 0


    }

    void "TestGetObjectNameFromFullyQualifiedObjectName" () {

        expect:

        "Program" == service.getObjectNameFromFullyQualifiedObjectName("org.hisp.dhis.program.Program")
        "ProgramTrackedEntityAttribute" == service.getObjectNameFromFullyQualifiedObjectName("org.hisp.dhis.program.ProgramTrackedEntityAttribute")
        "Program" == service.getObjectNameFromFullyQualifiedObjectName("Program")
        null == service.getObjectNameFromFullyQualifiedObjectName(null)
        "" == service.getObjectNameFromFullyQualifiedObjectName("")



    }

    void "TestGetPluralizedObjectNameFromObjectName" () {

        expect:

        "programs" == service.getPluralizedObjectNameFromObjectName("Program")
        "programTrackedEntityAttributes" == service.getPluralizedObjectNameFromObjectName("ProgramTrackedEntityAttribute")
        "" == service.getPluralizedObjectNameFromObjectName("")
        null == service.getPluralizedObjectNameFromObjectName(null)
        "as" == service.getPluralizedObjectNameFromObjectName("A")
    }

}
