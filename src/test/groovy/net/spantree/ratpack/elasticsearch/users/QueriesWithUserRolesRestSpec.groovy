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

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.http.client.RequestSpec
import ratpack.test.http.TestHttpClient
import ratpack.test.http.TestHttpClients
import ratpack.test.ApplicationUnderTest
import ratpack.test.remote.RemoteControl
import spock.lang.Specification

class QueriesWithUserRolesRestSpec extends Specification {
    ApplicationUnderTest aut = new LocalScriptApplicationUnderTest('other.remoteControl.enabled': 'true')
    @Delegate TestHttpClient client = TestHttpClients.testHttpClient(aut)
    RemoteControl remote = new RemoteControl(aut)

    def cleanup() {
        remote.exec {
            get(EsaUserRepository).deleteIndex()
            true //We need to return a Serializable value.
        }
    }

    def "should not allow query if user role is not allowed"() {
        given:
        def json = new JsonSlurper()

        when:
        requestSpec{ RequestSpec requestSpec ->
            requestSpec.body.type("application/json")
            requestSpec.body.text(JsonOutput.toJson([
                    user: "paul",
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
            ]))
        }
        post("locations/_search")

        then:
        def locations = json.parseText(response.body.text)
        response.statusCode == 401
        !locations
    }

    def "should allow query if user role is allowed"() {
        given:
        def json = new JsonSlurper()

        when:
        requestSpec{ RequestSpec requestSpec ->
            requestSpec.body.type("application/json")
            requestSpec.body.text(JsonOutput.toJson([
                    user: "ringo",
                    fields: ['name', 'description'],
                    aggs: [
                            name: [
                                    terms: [ field: 'name' ]
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
            ]))
        }
        post("locations/_search")

        then:
        def locations = json.parseText(response.body.text)
        response.statusCode == 200
        locations["_shards"].containsKey("failed")
        locations["_shards"].containsKey("successful")
        locations["_shards"].containsKey("total")
        locations["hits"]["hits"].size() == 10
        locations["aggregations"]["name"]["buckets"].size() > 0
        !locations["timed_out"]
    }
}
