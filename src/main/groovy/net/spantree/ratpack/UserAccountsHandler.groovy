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
import net.spantree.esa.UserEntity
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

import static ratpack.jackson.Jackson.json

/**
 * Handler for listing user accounts
 */
class UserAccountsHandler extends GroovyHandler {
    UserDAO userDAO

    @Inject
    UserAccountsHandler(UserDAO userDAO) {
        this.userDAO = userDAO
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with {
            byMethod {
                get {
                    blocking {
                        userDAO.listAll()
                    } then { List<UserEntity> users ->
                        render json(users)
                    }
                }

                post {
                    blocking {
                        userDAO.create(request.body.text)
                    } then { UserEntity userEntity ->
                        render(json([user: userEntity]))
                    }
                }
            }
        }
    }
}
