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

package net.spantree.ratpack.elasticsearch

import net.spantree.esa.EsaPermissions
import net.spantree.esa.EsaSearchResponse
import net.spantree.ratpack.elasticsearch.users.EsaUserRepository
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.internal.InternalSearchHit
import spock.lang.Specification

class ElasticsearchQuerySpec extends Specification {
    ElasticsearchQuery elasticsearchQuery
    ElasticsearchClientService elasticsearchClientService
    EsaUserRepository esaUserRepository

    def setup() {
        File configFile = new File("src/ratpack/config", "EsaSampleConfig.groovy")
        def config = new ElasticsearchConfig(configFile)
        elasticsearchClientService = new ElasticsearchClientServiceImpl(config)
        def basicPermissions = new EsaPermissions()
        basicPermissions.base = [
                indices: [
                        freebase: [
                            access: "allow"
                        ]
                ]
        ]
        esaUserRepository = new EsaUserRepository(elasticsearchClientService)
        elasticsearchQuery = new ElasticsearchQuery(elasticsearchClientService, basicPermissions, esaUserRepository)
    }

    def "should proxy request to elasticsearch"() {
        given:
        def params = [
                fields: ['name', 'description'],
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

        when:
        EsaSearchResponse response = elasticsearchQuery.send("freebase", params)
        SearchResponse result = response.body

        then:
        !response.unauthorized
        result.took
        result.hits.hits.size() == 10
        result.aggregations.get("directed_by")["buckets"].size() == 10
        result.aggregations.get("genre")["buckets"].size() == 10
    }

    def "should not allow a query if its default access level is deny"() {
        given: "A set of permissions with default access level equal to 'deny'"
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        freebase: [
                                access: "deny"
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)

        and:
        def queryParams = [
                fields: [
                        returned: ['name', 'description'],
                        searched: ['name', 'description']
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


        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)

        then:
        searchResponse.unauthorized
        !searchResponse.body

    }

    def "should allow query if default access level is allow"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow"
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)

        and:
        def queryParams = [
                fields: ['name', 'description'],
                _source: [
                        include: [ "directed_by" ]
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

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)
        def directedBy = searchResponse.body.hits.hits.findAll{ InternalSearchHit hit ->
            hit.source && hit.source["directed_by"]
        }

        then:
        !searchResponse.unauthorized
        searchResponse.body
        directedBy.size() > 0
    }

    def "should not allow source_filters if they are not specified" () {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [ _default: [ access: "allow", source_filters: ["director.*"] ] ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)

        and:
        def queryParams = [
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

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)
        def directedBy = searchResponse.body.hits.hits.findAll{ InternalSearchHit hit ->
            hit.source && hit.source["directed_by"]
        }

        then:
        !searchResponse.unauthorized
        searchResponse.body.hits.hits.size() > 0
        directedBy.size() == 0

    }

    def "should allow source_filters if they are specified" () {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                source_filters: [
                                        "directed_by.*"
                                ]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions, esaUserRepository)

        and:
        def queryParams = [
                fields: ['name', 'description'],
                _source: [
                        include: [ "directed_by" ]
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

        when:
        EsaSearchResponse searchResponse = esaQuery.send("freebase", queryParams)
        def directedBy = searchResponse.body.hits.hits.findAll{ InternalSearchHit hit ->
            hit.source && hit.source["directed_by"]
        }

        then:
        !searchResponse.unauthorized
        searchResponse.body.hits.hits.size() > 0
        searchResponse.body.hits.hits*.source
        directedBy.size() > 0

    }
}
