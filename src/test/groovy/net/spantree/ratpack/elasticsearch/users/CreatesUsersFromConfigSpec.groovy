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

import net.spantree.ratpack.elasticsearch.ElasticsearchClientBaseSpec

class CreatesUsersFromConfigSpec extends ElasticsearchClientBaseSpec {
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

    def "should create a user"() {
        given:
        def user = [username: "davidBowie", roles: ["GENERAL"]]

        when:
        def created = esaUserRepository.create(user)

        then:
        created
    }

    def "should retrieve a user given its username"() {
        when:
        def esaUser = esaUserRepository.findByUsername("sampleUser")

        then:
        esaUser.username == "sampleUser"
    }

    def "should create users in bulk"() {
        given:
        def users = [
                [username: "john", roles: ["SINGER"]],
                [username: "ringo", roles: ["DRUMMER"]],
                [username: "paul", roles: ["SINGER", "GUITAR"]],
                [username: "george", roles: ["SINGER", "GUITAR"]]
        ]

        when:
        esaUserRepository.bulkCreate(users)
        elasticsearchClientService.client.admin().indices().prepareRefresh(INDEX_NAME).execute().actionGet()

        and:
        def retrievedUsers = esaUserRepository.all()

        then:
        retrievedUsers.size() == users.size() + 1
    }
}
