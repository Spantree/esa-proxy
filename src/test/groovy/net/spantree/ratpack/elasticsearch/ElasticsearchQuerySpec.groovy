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

import net.spantree.esa.UserDAO
import net.spantree.esa.UserEntity
import net.spantree.esa.persistence.MongoClient
import org.elasticsearch.action.search.SearchResponse
import spock.lang.Specification

class ElasticsearchQuerySpec extends Specification {
    ElasticsearchQuery elasticsearchQuery
    UserDAO userDAO

    def setup() {
        def elasticsearchClientService = Stub(ElasticsearchClientService) {
            query() >> new SearchResponse()
        }
        userDAO = new UserDAO(new MongoClient(new File("src/ratpack/config", "Config.groovy")))
        elasticsearchQuery = new ElasticsearchQuery(elasticsearchClientService, userDAO)
    }

    def "should not be authorized to make a query without an apiToken"() {
        given:
        def params = [:]

        when:
        def result = elasticsearchQuery.send("indexName", params)

        then:
        result.unauthorized
        !result.body
    }

    def "should not be allowed to make a query with an invalid apiToken"() {
        given:
        def params = [apiToken: "Invalid Api Token"]

        when:
        def result = elasticsearchQuery.send("indexName", params)

        then:
        result.unauthorized
        !result.body
    }

    def "should make query if token is valid"() {
        given:
        UserEntity user = userDAO.create("{'name': 'Freddy Krueger'}")

        and:
        def query = [apiToken: user.apiToken]

        when:
        def result = elasticsearchQuery.send("indexName", query)

        then:
        !result.unauthorized
        result.body
    }


}
