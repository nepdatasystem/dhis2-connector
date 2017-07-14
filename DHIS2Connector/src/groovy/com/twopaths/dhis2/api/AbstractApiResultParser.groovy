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

import org.apache.commons.lang.StringUtils

/**
 * Abstract super class for parsing DHIS 2 API Results based off of DHIS 2 API version.
 * Provides helper method(s) to be used by parsers
 */
abstract class AbstractApiResultParser implements ApiResultParser {

    final String ERROR = "ERROR"
    final String WARNING = "WARNING"
    final String UNKNOWN = "UNKNOWN"

    int getSucceededCount(ApiActionType action, def importCount) {
        def succeeded = 0

        if (importCount) {
            switch (action) {
            // Some POSTs allow update and delete so need to check the imported and updated count...
                case ApiActionType.Import:
                    succeeded = (importCount?.imported ?: 0) + (importCount?.updated ?: 0) + (importCount?.deleted ?:0)
                    break
                case ApiActionType.Update:
                    succeeded = importCount?.updated ?: 0
                    break
                case ApiActionType.Delete:
                    succeeded = importCount?.deleted ?: 0
                    break
            }
        }
        return succeeded
    }

    /*
     *  The parsers need to extract the object name from the fully qualified name that is used in the API response
     *  EG:
     *  Object Name: "Program"
     *  Fully Qualified Object Name: "org.hisp.dhis.program.Program"
     */
    String getObjectNameFromFullyQualifiedObjectName (String objectName) {
        if (!objectName) {
            return objectName
        } else {
            return objectName.contains(".") ? StringUtils.substringAfterLast(objectName, ".") : objectName
        }
    }

    /*
     *  The parsers need to extract the pluralized object name that is used in the API request
     *  EG:
     *  Object Name: "Program"
     *  Pluralized Name: "programs"
     */
    String getPluralizedObjectNameFromObjectName (String objectName) {
        if (!objectName || objectName.length() == 0) {
            return objectName
        } else {
            return objectName.substring(0,1).toLowerCase() + objectName.substring(1, objectName.length()) + "s"
        }
    }
}
