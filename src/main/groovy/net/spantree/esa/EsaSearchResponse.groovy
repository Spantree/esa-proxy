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

package net.spantree.esa

import org.elasticsearch.action.search.SearchResponse

/**
 * A value object that wraps the response from an Elasticsearch query.
 * We can check if the executeDocument sent was unauthorized by inspecting the
 * unauthorized field. If the request was authorized, the result of the
 * executeDocument will be saved in the body field.
 */
class EsaSearchResponse {
    Boolean unauthorized
    SearchResponse body
}
