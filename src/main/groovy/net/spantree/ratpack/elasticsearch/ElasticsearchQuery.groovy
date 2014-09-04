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

    private XContentBuilder toXContentBuilder(String indexName, Map node) {
        XContentBuilder doc = XContentFactory.jsonBuilder().startObject()
        if(node) {
            node.each{ String key, value ->
                switch(key) {
                    case "user":
                        break
                    case "_source":
                        value["includes"] = applySourceFilters(indexName, value["include"])
                        if(value["includes"].size() == 0) {
                            value.remove("includes")
                            value["excludes"] = ["*"]
                        }
                        doc = addField(key, value, doc)
                        break
                    case "fields":
                        value = applyFieldFilters(indexName, value)
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

    EsaSearchResponse send(String indexName, Map params) {
        EsaSearchResponse searchResponse = new EsaSearchResponse()
        if(defaultAccessLevel(indexName)) {
            if(hasRoleAccess(params.user, indexName)) {
                searchResponse.unauthorized = Boolean.FALSE
                XContentBuilder doc = toXContentBuilder(indexName, params)
                searchResponse.body = elasticsearchClientService.executeDocument(indexName, doc)
            } else {
                searchResponse.unauthorized = Boolean.TRUE
            }
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

    List<String> applySourceFilters(String indexName, List<String> sourceFilters) {
        List<String> _sourceFilters = []
        List<String> result = []
        if(esaPermissions.base.indices.containsKey(indexName)) {
            _sourceFilters << getSourceFilters(indexName)
        } else {
            if(getSourceFilters("_default")) {
                _sourceFilters << getSourceFilters("_default")
            }
        }
        if(_sourceFilters.size() > 0) {
            result = _sourceFilters.findAll{ String filter ->
                sourceFilters?.contains(filter?.tokenize(".")?.first())
            }
        } else {
          result = sourceFilters
        }
        result.collect{ String res ->
            res.tokenize(".").first()
        }
    }

    List<String> applyFieldFilters(String indexName, List<String> fields) {
        List<String> fieldsToFilterBy = []
        if(esaPermissions.base.indices.containsKey(indexName)) {
            fieldsToFilterBy = getFieldFilters(indexName).findAll { String fieldFilter ->
                fields.contains(fieldFilter)
            }
        } else {
            fieldsToFilterBy = getFieldFilters("_default").findAll { String fieldFilter ->
                fields.contains(fieldFilter)
            }
        }
        fieldsToFilterBy
    }

    private List<String> getSourceFilters(String indexName) {
        esaPermissions.base.indices[indexName]["source_filters"]
    }

    private List<String> getFieldFilters(String indexName) {
        esaPermissions.base.indices[indexName]["fields"]
    }

    private Boolean hasRoleAccess(String username, String indexName) {
        Boolean defaultAccess = Boolean.TRUE
        if(esaPermissions.base.indices?.containsKey(indexName)){
            if(hasRoleRestrictions(indexName)) {
                EsaUser esaUser = esaUserRepository.findByUsername(username)
                defaultAccess = esaPermissions.base.indices[indexName]["roles"].findAll { String role ->
                    esaUser.roles?.contains(role)
                }.size() > 0
            }
        } else {
            if(hasRoleRestrictions("_default")) {
                EsaUser esaUser = esaUserRepository.findByUsername(username)
                defaultAccess = esaPermissions.base.indices["_default"]["roles"].findAll { String role ->
                    esaUser.roles?.contains(role)
                }.size() > 0
            }
        }
        defaultAccess
    }

    private Boolean hasRoleRestrictions(String indexName) {
        esaPermissions.base.indices[indexName].containsKey("roles") && esaPermissions.base.indices[indexName]["roles"].size() > 0
    }

}
