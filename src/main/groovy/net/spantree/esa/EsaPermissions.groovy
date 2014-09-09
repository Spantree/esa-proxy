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
 * A configuration object for the ESA proxy configuration. At startup, the EsaPermissions.js file is read, and its
 * content is stored in an instance of this object. We can then query the configuration for configuration  settings
 * by indexName and/or user roles.
 */
class EsaPermissions {
    Map base
    List<Map<String, List<String>>> users

    /**
     * Queries for a configuration by index name and/or user role.
     * For example: esaPermissionsInstance.where indexName: "someIndex", roles: ["RoleA", "RoleB"]
     * @param query
     * @return EsaPermissionConfiguration for any matching setting.
     */
    EsaPermissionConfiguration where(Map<String, String> query) {
        String indexName = getConfigIndexName(query.indexName)
        getIndexConfiguration(indexName, query)
    }

    private EsaPermissionConfiguration getIndexConfiguration(String indexName, Map<String, String> query) {
        EsaPermissionConfiguration config
        if(base.indices[indexName].size() > 0) {
            if(query.containsKey("roles") && query.roles?.size() > 0) {
                config = EsaPermissionConfiguration.create(getEntryWithRoles(indexName, query.roles))
            } else {
                config = EsaPermissionConfiguration.create(getEntryWithoutRoles(indexName))
            }
        } else {
            config = EsaPermissionConfiguration.create([:])
        }
        config
    }

    private String getConfigIndexName(String indexName) {
        if(base.indices.containsKey(indexName)) {
            return indexName
        } else {
            return "_default"
        }
    }

    def getEntryWithoutRoles(String indexName) {
        base.indices[indexName].find{ Map configEntry ->
            !configEntry.containsKey("roles")
        }
    }

    def getEntryWithRoles(String indexName, List<String> roles) {
        base.indices[indexName].find{ Map configEntry ->
            //Rhino does not cast properly, so we have to make sure that we cast
            //all the roles to a string so we can compare them.
            hasAnyRole(configEntry["roles"]?.collect{it.toString()}, roles)
        }
    }

    private boolean hasAnyRole(List<String> allowedRoles, List<String> roles) {
        allowedRoles?.intersect(roles)?.size() > 0
    }
}
