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

package net.spantree.ratpack.elasticsearch.users

import net.spantree.esa.EsaPermissions
import net.spantree.esa.EsaSearchResponse
import net.spantree.ratpack.elasticsearch.ElasticsearchClientBaseSpec
import net.spantree.ratpack.elasticsearch.ElasticsearchQuery

class QueriesWithUserRolesRulesSpec extends ElasticsearchClientBaseSpec {
    private final String INDEX_NAME = "esa_users"
    EsaUserRepository esaUserRepository

    def setup(){
        esaUserRepository = new EsaUserRepository(elasticsearchClientService)
        if(elasticsearchClientService.indexExists(INDEX_NAME)) {
            esaUserRepository.deleteIndex()
        }
        esaUserRepository.createIndex()
        esaUserRepository.create(username: "sampleUser", roles: ["GENERAL"])
        elasticsearchClientService.client.admin().indices().prepareRefresh(INDEX_NAME).execute().actionGet()
    }

    def cleanup() {
        esaUserRepository.deleteIndex()
    }

    def "should not allow query if user role is not allowed"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                fields: ["directed_by", "produced_by"],
                                roles: ["DRUMMER"]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)
        def queryParams = getQueryParams()
        queryParams["user"] = "sampleUser"

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)

        then:
        searchResponse.unauthorized
    }

    def "should allow query if user role is allowed"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                fields: ["directed_by", "produced_by"],
                                roles: ["DRUMMER", "GENERAL"]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)
        def queryParams = getQueryParams()
        queryParams["user"] = "sampleUser"

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)

        then:
        !searchResponse.unauthorized
        searchResponse.body.hits.hits
    }

    def "should not allow query if role is specified but user is absent"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                fields: ["directed_by", "produced_by"],
                                roles: ["DRUMMER", "GENERAL"]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)
        def queryParams = getQueryParams()

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)

        then:
        searchResponse.unauthorized
        !searchResponse.body
    }

    def "should not allow query if role is specified but an invalid username is provided"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                fields: ["directed_by", "produced_by"],
                                roles: ["DRUMMER", "GENERAL"]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)
        def queryParams = getQueryParams()
        queryParams["user"] = "invalidUsername"

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)

        then:
        searchResponse.unauthorized
        !searchResponse.body
    }

    def getQueryParams() {
        [
                fields: ['name', 'description'],
                _source: [
                        includes: [ "directed_by" ]
                ],
                aggs: [
                        genre: [
                                terms: [ field: 'genre.facet' ]
                        ],
                        directed_by: [
                                terms: [ field: 'directed_by.facet' ]
                        ]
                ],
                highlight: [
                        pre_tags: ['<mark>'],
                        post_tags: ['</mark>'],
                        fields: [
                                name: [ number_of_fragments: 0 ],
                                description: [ number_of_fragments: 0 ]
                        ]
                ]
        ]
    }

}
