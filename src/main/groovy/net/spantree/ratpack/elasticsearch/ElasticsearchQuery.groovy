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

import net.spantree.esa.EsaPermissions
import net.spantree.esa.EsaSearchResponse
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory

import javax.inject.Inject

class ElasticsearchQuery {
    private final ElasticsearchClientService elasticsearchClientService
    private final EsaPermissions esaPermissions

    @Inject
    ElasticsearchQuery(ElasticsearchClientService elasticsearchClientService, EsaPermissions esaPermissions) {
        this.elasticsearchClientService = elasticsearchClientService
        this.esaPermissions = esaPermissions
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

    EsaSearchResponse send(String indexName, Map params) {
        EsaSearchResponse searchResponse = new EsaSearchResponse()
        if(defaultAccessLevel(indexName)) {
            searchResponse.unauthorized = Boolean.FALSE
            searchResponse.body = elasticsearchClientService.query(indexName, toXContentBuilder(params))
        } else {
            searchResponse.unauthorized = Boolean.TRUE
        }
        searchResponse
    }

    Boolean defaultAccessLevel(String indexName) {
        Boolean accessLevel = Boolean.FALSE
        if(esaPermissions.base.indices.containsKey(indexName)) {
            switch(esaPermissions.base?.indices[indexName]["access"]) {
                case "allow":
                    accessLevel = Boolean.TRUE
                    break
                default:
                    accessLevel = Boolean.FALSE
            }
        } else {
            switch(esaPermissions.base.indices["_default"]["access"]) {
                case "allow":
                    accessLevel = Boolean.TRUE
                    break
                default:
                    accessLevel = Boolean.FALSE
            }
        }

        accessLevel
    }

}
