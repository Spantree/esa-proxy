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

package net.spantree.ratpack.elasticsearch

import net.spantree.esa.UserDAO
import net.spantree.esa.UserEntity
import net.spantree.esa.persistence.MongoClient
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients
import ratpack.test.ApplicationUnderTest
import spock.lang.Specification

class EsaProxyHandlerSpec extends Specification {
    UserDAO userDAO
    ApplicationUnderTest aut = new LocalScriptApplicationUnderTest('other.remoteControl.enabled': 'true')
    @Delegate TestHttpClient client = TestHttpClients.testHttpClient(aut)

    def setup() {
        userDAO = new UserDAO(new MongoClient(new File("src/ratpack/config", "Config.groovy")))
    }

    def cleanup() {
        userDAO.dropDatabase()
    }

    def "lists ten items by default"() {
        given:
        UserEntity user = userDAO.create("{'name': 'Kermit The Frog'}")
        when:
        request.contentType("application/json")
            .body([
                apiToken: user.apiToken,
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
            ])
        post("freebase/_search")

        then:
        with(response.jsonPath()) {
            getMap("hits").hits.size() == 10
            !getBoolean("timed_out")
            getInt("took") > 0
            getMap("_shards").containsKey("failed")
            getMap("_shards").containsKey("successful")
            getMap("_shards").containsKey("total")
            getMap("aggregations").directed_by.buckets.size() > 0
            getMap("aggregations").genre.buckets.size() > 0
        }
    }

    def "returns 401 if request is made without an api token"() {
        when:
        request.contentType("application/json")
                .body([
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
        ])
        post("freebase/_search")

        then:
        with(response.jsonPath()){
            getString("error") == "Unauthorized request!"
        }
        response.statusCode == 401
    }

}
