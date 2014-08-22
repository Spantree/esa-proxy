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

package net.spantree.ratpack

import net.spantree.esa.UserDAO
import net.spantree.esa.persistence.MongoClient
import ratpack.groovy.test.LocalScriptApplicationUnderTest
import ratpack.groovy.test.TestHttpClient
import ratpack.groovy.test.TestHttpClients
import ratpack.test.ApplicationUnderTest
import spock.lang.Specification

class UserAccountsHandlerSpec extends Specification {
    UserDAO userDAO
    ApplicationUnderTest aut = new LocalScriptApplicationUnderTest()
    @Delegate TestHttpClient client = TestHttpClients.testHttpClient(aut)

    def setup() {
        userDAO = new UserDAO(new MongoClient(new File("src/ratpack/config", "Config.groovy")))
        userDAO.dropDatabase()
        userDAO.create("{'name': 'Pee Wee'}")
        userDAO.create("{'name': 'Jonny Kash'}")
    }

    def cleanup() {
        userDAO.dropDatabase()
    }


    def  "should return the list of user accounts"() {
        when:
        get("esa/user_accounts")

        then:
        with(response.body.jsonPath()) {
            getList("users").size() == 2
        }
    }

    def "creates a user with an api token"() {
        when:
        request.contentType("application/json")
                .body([name: "Rand Fluke"])

        post("esa/user_accounts")

        then:
        with(response.jsonPath()) {
            getMap("user").name == "Rand Fluke"
            getMap("user").apiToken
        }
    }
}
