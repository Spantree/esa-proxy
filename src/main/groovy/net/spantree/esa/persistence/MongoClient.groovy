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

package net.spantree.esa.persistence

import com.mongodb.DB
import com.mongodb.DBCollection
import com.mongodb.Mongo
import net.spantree.esa.EsaEnvironment

class MongoClient implements DatabaseClient {
    private ConfigObject config
    private Mongo _mongo

    MongoClient(File configurationFile){
        config = new ConfigSlurper().parse(configurationFile.text)
    }

    Mongo getMongo() {
        if(_mongo == null) {
            _mongo = new Mongo(config.app.mongo.test.host as String, config.app.mongo.test.port ?: 27017)
        }
        _mongo
    }

    DB getDatabase() {
        DB database
        switch(EsaEnvironment.currentEnvironment) {
            case "DEVELOPMENT":
                database = mongo.getDB(config.app.mongo.development.database as String)
                break
            case "PRODUCTION":
                database = mongo.getDB(config.app.mongo.production.database as String)
                break
            default:
                database = mongo.getDB(config.app.mongo.test.database as String)
        }
        database
    }

    DBCollection getCollection(String collectionName) {
        database.getCollection(collectionName)
    }

    def dropDatabase() {
        database.dropDatabase()
    }
}
