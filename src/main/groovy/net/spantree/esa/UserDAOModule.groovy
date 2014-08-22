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

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import net.spantree.esa.persistence.MongoClient
import javax.inject.Singleton

class UserDAOModule extends AbstractModule {
    private static ConfigObject config
    private final String databaseName
    private File cfgFile

    UserDAOModule(File cfgFile) {
        this.cfgFile = cfgFile
        config = new ConfigSlurper().parse(cfgFile.text)
        switch(EsaEnvironment.currentEnvironment) {
            case "DEVELOPMENT":
                databaseName = config.app.mongo.development.database as String
                break
            case "PRODUCTION":
                databaseName = config.app.mongo.production.database as String
                break
            default:
                databaseName = config.app.mongo.test.database as String
        }
    }

    @Override
    protected void configure() {
        bind(UserDAO.class).in(Scopes.SINGLETON)
    }

    @Provides
    @Singleton
    MongoClient providesMongoClient() {
        new MongoClient(cfgFile)
    }
}
