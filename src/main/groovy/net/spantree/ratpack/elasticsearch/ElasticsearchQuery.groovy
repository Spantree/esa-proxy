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
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory

import javax.inject.Inject

class ElasticsearchQuery {
    ElasticsearchClientService elasticsearchClientService
    UserDAO userDAO

    @Inject
    ElasticsearchQuery(ElasticsearchClientService elasticsearchClientService, UserDAO userDAO) {
        this.elasticsearchClientService = elasticsearchClientService
        this.userDAO = userDAO
    }

    private XContentBuilder addField( fieldName,  value, XContentBuilder doc) {
        switch(fieldName) {
            case "fields":
                doc.array(fieldName, value.collect{ it.toString() }.toArray())
                break
            case "apiToken":
                break
            default:
                doc.field(fieldName, value)
        }
        doc
    }

    private XContentBuilder toXContentBuilder(Map node) {
        XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
        node.each{ String key, value ->
            doc = addField(key, value, doc)
        }

        doc.endObject()
        doc
    }

    def send(String indexName, Map params) {
        boolean unauthorized = true
        def body
        UserEntity userEntity = userDAO.findByApiToken(params.apiToken.toString())
        if(userEntity.id) {
            unauthorized = false
            SearchResponse response = elasticsearchClientService.query(indexName, toXContentBuilder(params))
            body = response
        }
        [unauthorized: unauthorized, body: body]
    }

}
