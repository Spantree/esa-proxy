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

import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.util.JSON
import net.spantree.esa.persistence.MongoClient
import org.bson.types.ObjectId

import javax.inject.Inject

/**
 * Data Access Object for the user collection.
 */
class UserDAO {
    private final String COLLECTION_NAME = "users"
    private MongoClient mongoClient
    DBCollection collection

    @Inject
    UserDAO(MongoClient mongoClient) {
       this.mongoClient = mongoClient
       collection = this.mongoClient.getCollection(COLLECTION_NAME)
    }

    /**
     * Creates a user document.
     * @param json String representation of the user document we want to create.
     * @return UserEntity instance of the document created.
     */
    public UserEntity create(String json) {
        BasicDBObject doc = JSON.parse(json) as BasicDBObject
        doc.append("apiToken", UUID.randomUUID().toString())
        collection.insert(doc)
        new UserEntity(
            doc.get("_id").toString(),
            doc.get("name"),
            doc.get("apiToken"),
            doc.get("google"),
            doc.get("facebook"),
            doc.get("twitter")
        )
    }

    /**
     * Retrieves a user document that matches the specified id.
     * @param id
     * @return UserEntity
     */
    UserEntity getById(String id) {
        BasicDBObject obj = [_id: new ObjectId(id)] as BasicDBObject
        DBObject result = collection.findOne(obj)
        new UserEntity(
            result.get("_id").toString(),
            result.get("name"),
            result.get("apiToken"),
            result.get("google"),
            result.get("facebook"),
            result.get("twitter")
        )
    }

    /**
     * Retrieves a user document with the specified api token.
     * @param apiToken
     * @return
     */
    UserEntity findByApiToken(String apiToken) {
        BasicDBObject obj = [apiToken: apiToken] as BasicDBObject
        DBObject result = collection.findOne(obj)
        if(result) {
            return new UserEntity(
                    result.get("_id").toString(),
                    result.get("name"),
                    result.get("apiToken"),
                    result.get("google"),
                    result.get("facebook"),
                    result.get("twitter")
            )
        }
        return new UserEntity()
    }

    def dropDatabase() {
        this.mongoClient.dropDatabase()
    }

    List<UserEntity> listAll() {
        List<UserEntity> users = []
        DBCursor cursor = collection.find()
        try {
            while(cursor.hasNext()) {
                DBObject result = cursor.next()
                UserEntity user = new UserEntity(
                    result.get("_id").toString(),
                    result.get("name"),
                    result.get("apiToken"),
                    result.get("google"),
                    result.get("facebook"),
                    result.get("twitter")
                )
                users << user
            }
        } finally {
            cursor.close()
        }

        users
    }

   def delete(String id) {
       collection.remove(new BasicDBObject("_id": new ObjectId(id)))
   }
}
