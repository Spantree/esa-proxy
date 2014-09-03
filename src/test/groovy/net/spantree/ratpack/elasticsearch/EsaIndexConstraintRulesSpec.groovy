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
import org.elasticsearch.search.internal.InternalSearchHit

class EsaIndexConstraintRulesSpec extends ElasticsearchClientBaseSpec {

    def "should deny access to an index if not explicitly allowed"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [access: "allow"],
                        freebase: []
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions)
        def queryParams = getQueryParams()

        when:
        EsaSearchResponse esaSearchResponse = esaQuery.send("freebase", queryParams)

        then:
        !esaSearchResponse.body
    }

    def "should deny access to an index if explicitly denied"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [access: "allow"],
                        freebase: [access: "deny"]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions)
        def queryParams = getQueryParams()

        when:
        EsaSearchResponse esaSearchResponse = esaQuery.send("freebase", queryParams)

        then:
        !esaSearchResponse.body
    }

    def "should allow access to an index if explicitly allowed"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [access: "allow"],
                        freebase: [access: "allow"]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions)
        def queryParams = getQueryParams()

        when:
        EsaSearchResponse esaSearchResponse = esaQuery.send("freebase", queryParams)

        then:
        esaSearchResponse.body.hits.hits.size() > 0
    }

    def "should allow access to specified fields"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow",
                                fields: ["name", "directed_by"]
                        ],
                        freebase: [
                                access: "allow",
                                fields: ["name"]
                        ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions)
        def queryParams = getQueryParams()

        when:
        EsaSearchResponse esaSearchResponse = esaQuery.send("freebase", queryParams)
        def filmNames = esaSearchResponse.body.hits.hits.findAll{ InternalSearchHit hit ->
            hit.field("name")
        }
        def directors = esaSearchResponse.body.hits.hits.findAll{ InternalSearchHit hit ->
            hit.field("directed_by")
        }

        then:
        esaSearchResponse.body.hits.hits
        !esaSearchResponse.unauthorized
        filmNames.size() > 0
        directors.size() == 0
    }


    def getQueryParams() {
        [
                fields: ['name', 'directed_by'],
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
