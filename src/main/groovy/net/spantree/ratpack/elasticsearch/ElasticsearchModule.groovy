/*
 *
 *   Copyright 2014 the original author or authors.
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
 *
 */

package net.spantree.ratpack.elasticsearch

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Scopes
import net.spantree.esa.EsaPermissions

import javax.inject.Singleton

/**
 * An Elasticsearch module that provides a way to create an index and populate types in that index.
 *
 */
class ElasticsearchModule extends AbstractModule {

    private File cfg

    ElasticsearchModule(File cfg) {
        this.cfg = cfg
    }

    @Override
    protected void configure() {
        bind(ElasticsearchQuery.class).in(Scopes.SINGLETON)
    }

    @Provides
    @Singleton
    ElasticsearchConfig provideElasticsearchConfig() {
        new ElasticsearchConfig(this.cfg)
    }

    @Provides
    @Singleton
    ElasticsearchClientService provideElasticsearchClientService() {
        new ElasticsearchClientServiceImpl(new ElasticsearchConfig(this.cfg))
    }

    @Provides
    @Singleton
    EsaPermissions provideEsaPermissions() {
        EsaPermissions esaPermissions = new EsaPermissions()
        esaPermissions.base = [
                indices: [
                        _default: [
                                access: "allow"
                        ]
                ]
        ]
        esaPermissions
    }
}
