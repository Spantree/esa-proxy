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

import javax.inject.Inject

class EsaUserRepository {
    private final String INDEX_NAME = "esa_users"
    private final ElasticsearchClientService elasticsearchClientService

    @Inject
    EsaUserRepository(ElasticsearchClientService elasticsearchClientService) {
        this.elasticsearchClientService = elasticsearchClientService
    }

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

    EsaUser findByUsername(String username) {
        def query = [
                bool: [
                        must: [[query_string: [query: username]]]
                ]
        ]
        XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
        doc.field("bool", query.bool)
        doc.endObject()
        SearchResponse searchResponse = elasticsearchClientService.query(INDEX_NAME, doc)
        new EsaUser( username: searchResponse.hits.hits.first().source["username"])
    }

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

    DeleteIndexResponse deleteIndex() {
        elasticsearchClientService.client.admin().indices().prepareDelete("esa_users").execute().actionGet()
    }

}
