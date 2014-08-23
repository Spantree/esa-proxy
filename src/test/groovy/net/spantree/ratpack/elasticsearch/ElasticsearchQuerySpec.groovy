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

import org.elasticsearch.action.search.SearchResponse
import spock.lang.Specification

class ElasticsearchQuerySpec extends Specification {
    ElasticsearchQuery elasticsearchQuery

    def setup() {
        File configFile = new File("src/ratpack/config", "EsSampleConfig.groovy")
        def config = new ElasticsearchConfig(configFile)
        ElasticsearchClientService elasticsearchClientService = new ElasticsearchClientServiceImpl(config)
        elasticsearchQuery = new ElasticsearchQuery(elasticsearchClientService)
    }

    def "should proxy request to elasticsearch"() {
        given:
        def params = [
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
        SearchResponse result = elasticsearchQuery.send("freebase", params)

        then:
        result
        result.took
        result.hits.hits.size() == 10
        result.aggregations.get("directed_by")["buckets"].size() == 10
        result.aggregations.get("genre")["buckets"].size() == 10
    }


}
