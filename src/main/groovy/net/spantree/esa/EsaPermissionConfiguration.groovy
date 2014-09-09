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

/**
 * A configuration setting for an index from EsaPermissions.js.
 */
class EsaPermissionConfiguration {
    private Map config
    private String access
    private List<String> roles
    private List<String> fields
    private List<String> sourceFilters

    static EsaPermissionConfiguration create(Map config) {
        String access = config?.access ?: "deny"
        List<String> roles = config?.roles
        List<String> fields = config?.fields
        List<String> sourceFilters = config?.source_filters
        new EsaPermissionConfiguration(
                access: access,
                roles: roles,
                fields: fields,
                sourceFilters: sourceFilters
        )
    }

    boolean isAccessible() {
        access == "allow"
    }

    boolean exists() {
        isAccessible()
    }

    String getAccess() {
        access
    }

    List<String> getFields() {
        fields
    }

    List<String> getRoles() {
        roles
    }

    List<String> getSourceFilters() {
        sourceFilters
    }

}
