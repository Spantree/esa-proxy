/*
 * Copyright 2014 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.spantree.esa

import spock.lang.Specification

class EsaEnvironmentSpec extends Specification {

    def cleanup() {
        System.setProperty("ESA_ENV", "TEST")
    }

    def "Default Environment is TEST if none is specified"() {
        expect:
        EsaEnvironment.currentEnvironment == "TEST"
    }

    def "Current Environment is determined by ESA_ENV system property"() {
        when:
        System.setProperty("ESA_ENV", "TEST")

        then:
        EsaEnvironment.currentEnvironment == "TEST"

        when:
        System.setProperty("ESA_ENV", "DEVELOPMENT")

        then:
        EsaEnvironment.currentEnvironment == "DEVELOPMENT"

        when:
        System.setProperty("ESA_ENV", "PRODUCTION")

        then:
        EsaEnvironment.currentEnvironment == "PRODUCTION"

    }
}
