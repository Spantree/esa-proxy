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

import groovy.json.JsonSlurper
import net.spantree.esa.EsaSearchResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import ratpack.groovy.handling.GroovyContext
import ratpack.groovy.handling.GroovyHandler

import javax.inject.Inject

import static ratpack.jackson.Jackson.json

class EsaProxyHandler extends GroovyHandler {
    private final ElasticsearchQuery elasticsearchQuery

    @Inject
    EsaProxyHandler(ElasticsearchQuery elasticsearchQuery) {
        this.elasticsearchQuery = elasticsearchQuery
    }

    @Override
    protected void handle(GroovyContext context) {
        context.with{
            byMethod {
                post {
                    blocking {
                        Map node = parse Map
                        println "node: ${node}"
                        if(!node) {
                            node = new JsonSlurper().parseText(request.body.text)
                        }
                        elasticsearchQuery.send(context.pathTokens.indexName, node)
                    } onError {
                        context.render json([message: "Something bad happened!"])
                    } then { EsaSearchResponse resp ->
                            if(resp.unauthorized) {
                                response.status(401)
                            }
                            println "search body: ${resp}"
                            context.render json(new JsonSlurper().parseText(resp.body.toString()))

                    }
                }
            }
        }
    }

}
