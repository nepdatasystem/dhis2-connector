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

package com.twopaths.dhis2.api

/**
 * Which version of the DHIS 2 API should be used
 */
enum ApiVersion {


    /* Version 2.23 of the API. As of 2.23, this is only used for the /api/metadata API call */
    DHIS2_VERSION_223 ("2.23", "/23"),

    /* Version 2.24 of the API. As of 2.24, the majority of the API has been versioned. See comments
     * in ApiResultParser224Service.groovy */
    DHIS2_VERSION_224 ("2.24", "/24"),

    /* Version 2.25 of the API
     */
    DHIS2_VERSION_225 ("2.25", "/25")


    private static Map<String, ApiVersion> lookup = new HashMap<String, ApiVersion>()

    static {
        for (ApiVersion apiVersion : ApiVersion.values()) {
            lookup.put(apiVersion.value(), apiVersion);
        }
    }

    private String name

    private String apiVersionSubPath

    private ApiVersion (String name, String apiVersionSubPath) {
        this.name = name
        this.apiVersionSubPath = apiVersionSubPath
    }

    public String value() {
        name
    }

    public String getApiVersionSubPath () {
        apiVersionSubPath
    }

    public static ApiVersion get(String name) {
        return lookup.get(name);
    }
}
