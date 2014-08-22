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

package net.spantree.esa

import net.spantree.esa.persistence.MongoClient
import spock.lang.Specification

class UserDAOSpec extends Specification {
    UserDAO userDAO

    def setup() {
        userDAO = new UserDAO(new MongoClient(new File("src/ratpack/config", "Config.groovy")))
    }

    def cleanup() {
        userDAO.dropDatabase()
    }

    def "saves a user record"() {
        given:
        String user = "{'name': 'Jon Doe'}"

        when:
        UserEntity userEntity = userDAO.create(user)

        then:
        userEntity.id
        userEntity.apiToken
    }

    def "can retrieve a user by its id"() {
        given: "A persisted user"
        UserEntity persistedUser = userDAO.create("{'name': 'Jane Smith'}")

        when:
        UserEntity retrievedUser = userDAO.getById(persistedUser.id)

        then:
        retrievedUser.id == persistedUser.id
        retrievedUser.apiToken == persistedUser.apiToken
    }

    def "can retrieve a user with a specified api token" () {
        given: "A persisted user"
        UserEntity persistedUser = userDAO.create("{'name': 'Dr. Seuss'}")

        when:
        UserEntity retrievedUser = userDAO.findByApiToken(persistedUser.apiToken)

        then:
        retrievedUser.id == persistedUser.id
    }

    def "should list all registered user accounts" () {
        given:
        userDAO.dropDatabase()

        and: "Two accounts are added"
        def firstUser = userDAO.create("{'name': 'First User'}")
        def secondUser = userDAO.create("{'name': 'Second User'}")

        when:
        def users = userDAO.listAll()

        then:
        users.size() == 2
        users.contains(firstUser)
        users.contains(secondUser)
    }

    def "should delete a user acount"() {
        given:
        def aUser = userDAO.create("{'name': 'Delete Me'}")

        when:
        userDAO.delete(aUser.id)

        then:
        !userDAO.findByApiToken(aUser.apiToken).id
    }

}
