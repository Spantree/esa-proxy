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


@Grab(group="org.codehaus.groovy.modules.http-builder", module="http-builder", version="0.7")

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import static groovyx.net.http.ContentType.JSON


def http = new HTTPBuilder("http://localhost:5051")

def postBody = [name: "John Wayne"]

http.request(Method.POST) {
    uri.path = "/esa/auth/register"
    requestContentType = JSON
    body = postBody

    response.success = { resp, reader ->
        println "Created user: ${reader.user}"
    }

    response.error = { resp ->
        println "Error: ${resp}"
    }
}
