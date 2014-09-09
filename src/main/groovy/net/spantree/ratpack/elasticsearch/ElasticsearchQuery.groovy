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

import net.spantree.esa.EsaPermissionConfiguration
import net.spantree.esa.EsaPermissions
import net.spantree.esa.EsaSearchResponse
import net.spantree.ratpack.elasticsearch.users.EsaUser
import net.spantree.ratpack.elasticsearch.users.EsaUserRepository
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory

import javax.inject.Inject

class ElasticsearchQuery {
    private final ElasticsearchClientService elasticsearchClientService
    private final EsaPermissions esaPermissions
    private final EsaUserRepository esaUserRepository

    @Inject
    ElasticsearchQuery(ElasticsearchClientService elasticsearchClientService, EsaPermissions esaPermissions, EsaUserRepository esaUserRepository) {
        this.elasticsearchClientService = elasticsearchClientService
        this.esaPermissions = esaPermissions
        this.esaUserRepository = esaUserRepository
    }

    private XContentBuilder addField( fieldName,  value, XContentBuilder doc) {
        switch(fieldName) {
            case "fields":
                doc.array(fieldName, value.collect{ it.toString() }.toArray())
                break
            case "apiToken":
                break
            case "from":
                doc.field(fieldName, new BigDecimal(value.toString()))
                break
            case "size":
                doc.field(fieldName, new BigDecimal(value.toString()))
                break
            default:
                doc.field(fieldName, value)

        }
        doc
    }

    private XContentBuilder toXContentBuilder(String indexName, Map node, EsaPermissionConfiguration indexConfiguration) {
        XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
        if(node) {
            node.each{ String key, value ->
                switch(key) {
                    case "user":
                        break
                    case "_source":
                        value["includes"] = applySourceFilters(value["include"], indexConfiguration)
                        if(value["includes"].size() == 0) {
                            value.remove("includes")
                            value["excludes"] = ["*"]
                        }
                        doc = addField(key, value, doc)
                        break
                    case "fields":
                        value = applyFieldFilters(value, indexConfiguration)
                        doc = addField(key, value, doc)
                        break
                    default:
                        doc = addField(key, value, doc)
                }
            }
        } else {
            doc  = addField("_source", [excludes: ["*"]], doc)
            doc = addField("fields", [], doc)
        }
        doc.endObject()
        doc
    }

    EsaSearchResponse send(String indexName, Map query) {
        EsaSearchResponse searchResponse = new EsaSearchResponse()
        List<String> roles = []
        if(query.containsKey("user")) {
            EsaUser esaUser = esaUserRepository.findByUsername(query.user)
            roles = esaUser.roles
        }
        EsaPermissionConfiguration indexConfiguration = esaPermissions.where indexName: indexName, roles: roles
        if(indexConfiguration.isAccessible()) {
            XContentBuilder doc = toXContentBuilder(indexName, query, indexConfiguration)
            searchResponse.body = elasticsearchClientService.executeDocument(indexName, doc)
        } else {
            searchResponse.unauthorized = Boolean.TRUE
        }
        searchResponse
    }

    List<String> applySourceFilters(List<String> sourceFilters, EsaPermissionConfiguration indexConfiguration) {
        List<String> result = []
        if(indexConfiguration.sourceFilters?.size() > 0 && sourceFilters?.size() > 0) {
            result = indexConfiguration.sourceFilters.findAll { String filter ->
                sourceFilters.contains( filter?.tokenize(".")?.first() )
            }
        } else {
            result = sourceFilters
        }
        result.collect{ String res ->
            res?.tokenize(".")?.first()
        }
    }

    List<String> applyFieldFilters(List<String> fields, EsaPermissionConfiguration indexConfiguration) {
        List<String> fieldsToFilterBy = []
        if(indexConfiguration.fields?.size() > 0) {
            fieldsToFilterBy = indexConfiguration.fields.findAll { String fieldFilter ->
                fields.contains(fieldFilter)
            }
        }
        fieldsToFilterBy
    }

}
