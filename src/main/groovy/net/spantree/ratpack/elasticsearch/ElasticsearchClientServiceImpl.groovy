/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.spantree.ratpack.elasticsearch

import com.google.inject.Inject
import groovy.util.logging.Slf4j
import org.elasticsearch.action.index.IndexRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Requests
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.XContentBuilder

import javax.annotation.PreDestroy

/**
 * A service that allows us to setup an elasticsearch client and check if an index exists.
 */

@Slf4j
class ElasticsearchClientServiceImpl implements ElasticsearchClientService {
    TransportClient client
    ElasticsearchConfig elasticsearchConfig
    private String INET_ADDRESS = "localhost"
    private int PORT = 9300

    @Inject
    ElasticsearchClientServiceImpl(ElasticsearchConfig config){
        this.elasticsearchConfig = config
        if(elasticsearchConfig.props.cluster["host"]) INET_ADDRESS = elasticsearchConfig.props.cluster["host"]
        if(elasticsearchConfig.props.cluster["port"]) PORT = Integer.parseInt("${elasticsearchConfig.props.cluster["port"]}")
        String clusterName = elasticsearchConfig.props?.cluster?.name
        ImmutableSettings settings = ImmutableSettings.settingsBuilder().put("cluster.name", clusterName).build()
        client = new TransportClient(settings)
        client.addTransportAddress(new InetSocketTransportAddress(INET_ADDRESS, PORT))
    }

    @PreDestroy
    void destroy() {
        log.info "Closing client: $client"
        client?.close()
    }

    /**
     * Verifies if an index with the given name exists.
     * @param indexName The name of the index.
     * @return a boolean indicating if index exists.
     */
    boolean indexExists(String indexname) {
        client.admin().indices().exists(Requests.indicesExistsRequest(indexname)).get().exists
    }

    /**
     * Saves some data to an index in elasticsearch.
     * <pre>
     *     elasticsearchClientService.doIndex("theTargetIndex", "234134234er123123", "person", "{'first_name': 'Jon', 'last_name': 'Doe'}")
     * </pre>
     * @param indexName
     * @param id unique identifier of the document in elasticsearch.
     * @param type the type of the document
     * @param jsonString the data that will be indexed.
     * @return id string representation of record indexed in elasticsearch.
     */
    public String doIndex(String indexName, String id, String type, String jsonString) {
        IndexRequestBuilder indexRequestBuilder = client.prepareIndex(indexName, type)
        indexRequestBuilder = indexRequestBuilder.setSource(jsonString).setId(id).setRefresh(true)
        indexRequestBuilder.execute().actionGet().id
    }

    SearchResponse query(String indexName, XContentBuilder doc) {
        this.client
            .prepareSearch(indexName)
            .setSource(doc)
            .execute()
            .actionGet()
    }

}
