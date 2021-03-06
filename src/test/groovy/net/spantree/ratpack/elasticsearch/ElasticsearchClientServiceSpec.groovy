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

import spock.lang.Specification

class ElasticsearchClientServiceSpec extends Specification {
    ElasticsearchClientServiceImpl elasticsearchClientService
    ElasticsearchConfig config

    def setup() {
        File configFile = new File("src/ratpack/config", "EsaSampleConfig.groovy")
        config = new ElasticsearchConfig(configFile)
        elasticsearchClientService = new ElasticsearchClientServiceImpl(config)
    }

    def "Elasticsearch client succesfully starts up"() {
        expect:
        elasticsearchClientService.client.settings().asMap['cluster.name'] == config.props.cluster.name
        elasticsearchClientService.client.settings().asMap['node.client'].toBoolean() == true
    }
}
