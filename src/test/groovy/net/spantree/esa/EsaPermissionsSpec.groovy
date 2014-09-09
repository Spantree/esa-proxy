/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.spantree.esa

import spock.lang.Specification

class EsaPermissionsSpec extends Specification {

    def "should find a configuration by index name"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [],
                        someFunkyIndex: [[access: "allow"]]
                ]
        ]

        when:
        def indexConfiguration = esaPermissions.where(indexName: "someFunkyIndex")

        then:
        indexConfiguration.access == "allow"
    }

    def "should return an empty configuration if index exists but access level is not 'allow'" () {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        someFunkyIndex: []
                ]
        ]

        when:
        def indexConfiguration = esaPermissions.where(indexName: "someFunkyIndex")

        then:
        !indexConfiguration.exists()
    }

    def "should return the configuration by index name and user role"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                [access: "allow", roles: ["RoleB"], fields: ["G", "H"]],
                                [access: "allow", fields: ["G", "H"]]
                        ],
                        someFunkyIndex: [
                                [access: "allow", roles: ["RoleA"], fields: ["A", "B"]],
                                [access: "allow", roles: ["RoleC"], fields: ["C", "D"]],
                                [access: "allow", fields: ["E", "F"]]
                        ]
                ]
        ]

        when:
        def indexConfiguration = esaPermissions.where(indexName: "someFunkyIndex", roles: ["RoleA"])

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["A", "B"]

        when:
        indexConfiguration = esaPermissions.where indexName: "someFunkyIndex", roles: ["RoleC"]

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["C", "D"]

        when:
        indexConfiguration = esaPermissions.where indexName: "someFunkyIndex", roles: ["RoleA", "RoleB"]

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["A", "B"]

        when:
        indexConfiguration = esaPermissions.where indexName: "someFunkyIndex", roles: ["RoleA", "RoleC"]

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["A", "B"]

        when:
        indexConfiguration = esaPermissions.where indexName: "someFunkyIndex", roles: ["RoleB"]

        then:
        !indexConfiguration.exists()
        !indexConfiguration.fields

        when:"Querying an index that is not configured"
        indexConfiguration = esaPermissions.where indexName: "notConfiguredIndex", roles: ["RoleB"]

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["G", "H"]

        when: "Query with invalid role for an index that is not configured"
        indexConfiguration = esaPermissions.where indexName: "notConfiguredIndex", roles: ["RoleA"]

        then: "An invalid index configuration is returned"
        !indexConfiguration.exists()
        !indexConfiguration.fields

        when: "Query with no roles should return configuration for fields that have no role restrictions"
        indexConfiguration = esaPermissions.where indexName: "notConfiguredIndex"

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["G", "H"]

        when: "We want to search by index name and an empty list of roles"
        indexConfiguration = esaPermissions.where indexName: "notConfiguredIndex", roles: []

        then:
        indexConfiguration.exists()
        indexConfiguration.fields == ["G", "H"]

    }
}
