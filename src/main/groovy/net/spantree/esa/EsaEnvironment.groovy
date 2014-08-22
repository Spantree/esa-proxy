/*
 * Copyright 2014 the original author or authors.
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
 */

package net.spantree.esa

/**
 * EsaEnvironment is used to check the current environment the application is running as.
 * The application environment is set via system properties.
 *
 * <pre>
 *     System.setProperty("ESA_ENV", "DEVELOPMENT")
 * </pre>
 *
 * Only three environments are allowed:
 *  1. DEVELOPMENT
 *  2. PRODUCTION
 *  3. TEST
 *
 */

class EsaEnvironment {

    public static String getCurrentEnvironment() {
        String _env = "TEST"
        switch(System.getProperty("ESA_ENV")){
            case "DEVELOPMENT":
                _env = "DEVELOPMENT"
                break
            case "PRODUCTION":
                _env = "PRODUCTION"
                break
            default:
                _env = "TEST"
        }
        _env
    }

}
