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

class AppliesFieldsFiltersSpec extends ElasticsearchClientBaseSpec {

    def "should ignore terms filters that are not specified when access level is allow"() {
        given:
        def esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [ access: "allow" ]
                ]
        ]

        and:
        def esaQuery = new ElasticsearchQuery(elasticsearchClientService, esaPermissions)

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
        def fieldsReturned = searchResponse.body.hits.hits.findAll { InternalSearchHit hit ->
            hit.fields
        }

        then:
        searchResponse.body.hits.hits
        !searchResponse.unauthorized
        searchResponse.body.hits.hits.size() > 0
        searchResponse.body.hits.hits*.source
        directedBy.size() > 0
        fieldsReturned.size() == 0
    }
}
