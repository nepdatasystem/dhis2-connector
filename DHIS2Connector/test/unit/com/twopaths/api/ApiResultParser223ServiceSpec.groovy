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
@TestFor(ApiResultParser223Service)
class ApiResultParser223ServiceSpec extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test 223 metadata parser"() {

        def requestBody = [
                "programTrackedEntityAttributes":[
                    [
                        "id":"Biv0EB9qg4r"
                    ],
                    [
                        "id":"DHggN4sya9M"
                    ],
                    [
                        "id":"BJTMIu9kGB6"
                    ],
                    [
                        "id":"N7S2HZHusnT"
                    ]
                ],
               "programs":[
                   [
                       "id":"lgtr6votm3o",
                       "name":"TEST_PROGRAM",

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

    void "test 223 metadata parser with errors"() {

        def requestBody = [
                "programTrackedEntityAttributes":[
                        [
                                "id":"Biv0EB9qg4r"
                        ],
                        [
                                "id":"DHggN4sya9M"
                        ],
                        [
                                "id":"BJTMIu9kGB6"
                        ],
                        [
                                "id":"N7S2HZHusnT"
                        ]
                ],
                "programs":[
                        [
                                "id":"lgtr6votm3o",
                                "name":"TEST_PROGRAM",

                        ]
                ]
        ]
        // When there is an ERROR, there are no stats for ProgramTrackedEntityAttribute. The entire block is rolled back.
        // there is also no associated "objectReports"
        // have asked DHIS 2 dev list but they have not yet responded.
        def responseData = [
                "status": "ERROR",
                "stats": [
                    "total": 1,
                    "created": 0,
                    "updated": 0,
                    "deleted": 0,
                    "ignored": 1
                ],
                "typeReports": [
                        [
                            "klass": "org.hisp.dhis.program.ProgramTrackedEntityAttribute",
                            "stats": [
                                "total": 0,
                                "created": 0,
                                "updated": 0,
                                "deleted": 0,
                                "ignored": 0
                            ]
                        ],
                        [
                            "klass": "org.hisp.dhis.program.Program",
                            "stats": [
                                "total": 1,
                                "created": 0,
                                "updated": 0,
                                "deleted": 0,
                                "ignored": 1
                            ],
                            "objectReports": [
                                [
                                    "klass": "org.hisp.dhis.program.Program",
                                    "index": 0,
                                    "errorReports": [
                                        [
                                            "message": "Maximum length of property \"shortName\"is 50, but given length was 150.",
                                            "mainKlass": "org.hisp.dhis.program.Program",
                                            "errorKlass": "java.lang.String",
                                            "errorCode": "E4001"
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

}
