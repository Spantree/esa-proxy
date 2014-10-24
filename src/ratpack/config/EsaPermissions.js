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

var strArrType = Java.type("java.lang.String[]");
var mapArrType = Java.type("java.util.Map[]");

var base = {
  indices: {
      _default: Java.to([
          {access: "allow",
          fields: Java.to(["name"], strArrType),
          source_filters: Java.to(["directed_by"], strArrType)
      }], mapArrType ),
      locations: Java.to([{
          access: "allow",
          fields: Java.to(["about", "description", "name"], strArrType),
          source_filters: Java.to(["directed_by"], strArrType),
          roles: Java.to(["GUITAR", "DRUMMER"], strArrType)
      }], mapArrType)
  }
};

var users = Java.to([
    {username: "ringo", roles: Java.to(["DRUMMER"], strArrType)},
    {username: "george", roles: Java.to(["GUITAR", "VOCALS"], strArrType)},
    {username: "john", roles: Java.to(["GUITAR", "VOCALS"], strArrType)},
    {username: "paul", roles: Java.to(["BASS", "VOCALS"], strArrType)}
], mapArrType);