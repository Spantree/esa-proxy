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

import net.spantree.ratpack.elasticsearch.ElasticsearchClientService
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.search.internal.InternalSearchHit

import javax.inject.Inject

/**
 * Repository for creating and fetching Esa Users.
 * All Esa Users are stored in an "esa_users" index and are assigned roles. These roles can then be used
 * to restrict access to specific indices.
 */
class EsaUserRepository {
    private final String INDEX_NAME = "esa_users"
    private final ElasticsearchClientService elasticsearchClientService

    @Inject
    EsaUserRepository(ElasticsearchClientService elasticsearchClientService) {
        this.elasticsearchClientService = elasticsearchClientService
    }

    /**
     * Create an Esa User.
     * @param options A Map with the attributes for the user. These should be:
     * <ol>
     *     <li>username: A string representing the username.</li>
     *     <li>roles: A List of strings representing the user roles.</li>
     *</ol>
     * If these are not present, then we get a noop.
     * @return true if user is created successfully.
     */
    public boolean create(Map<String, Object> options) {
        if(options.containsKey("username") && options.containsKey("roles")) {
            XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
            doc.field("username", options.username)
            doc.field("roles", options.roles)
            doc.endObject()
            return elasticsearchClientService.insert(INDEX_NAME, "users", doc).isCreated()
        } else {
            false
        }
    }

    def bulkCreate(List<Map<String, Object>> users){
        users.each { user ->
            create(user)
        }
    }

    /**
     * Fetchs all the Esa Users.
     * @return List of Esa Users.
     */
    List<EsaUser> all() {
        def query = [
                bool: [
                        must: [[query_string: [query: "*"]]]
                ]
        ]
        XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
        doc.field("bool", query.bool)
        doc.endObject()
        SearchResponse searchResponse = elasticsearchClientService.query(INDEX_NAME, doc)
        searchResponse.hits.hits.collect{ InternalSearchHit hit ->
            new EsaUser(username: hit.source["username"], roles: hit.source["roles"] as List<String>)
        }
    }

    /**
     * Finds an esa user with the matching username
     * @param username
     * @return An instance of EsaUser. If no matches are found, its username and roles fields will be null.
     */
    EsaUser findByUsername(String username) {
        EsaUser user = new EsaUser()
        if(username) {
            def query = [
                    bool: [
                            must: [[query_string: [query: username]]]
                    ]
            ]
            XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
            doc.field("bool", query.bool)
            doc.endObject()
            SearchResponse searchResponse = elasticsearchClientService.query(INDEX_NAME, doc)
            if(searchResponse.hits.hits.size() > 0) {
                user = new EsaUser( username: searchResponse.hits.hits.first().source["username"], roles: searchResponse.hits.hits.first().source["roles"] as List<String>)
            }
        }
        user
    }

    /**
     * Creates in the "esa_users" index.
     * @return
     */
    CreateIndexResponse createIndex() {
        final CreateIndexRequestBuilder createIndexRequestBuilder = elasticsearchClientService.client.admin().indices().prepareCreate(INDEX_NAME)
        final XContentBuilder mappingBuilder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("users")
                        .startObject("properties")
                            .startObject("username")
                                .field("type", "string")
                            .endObject()
                            .startObject("roles")
                                .field("type", "string")
                            .endObject()
                        .endObject()
                    .endObject()
                .endObject()
        createIndexRequestBuilder.addMapping("users", mappingBuilder)
        CreateIndexResponse response = createIndexRequestBuilder.execute().actionGet()
        response
    }

    /**
     * Deletes the "esa_users" index.
     * @return
     */
    DeleteIndexResponse deleteIndex() {
        elasticsearchClientService.client.admin().indices().prepareDelete("esa_users").execute().actionGet()
    }

}
