ESA Elasticsearch Endpoint
--------------------------

Proxies requests to Elasticsearch. It replicates the http API, but adds an authorization layer.

## Prerequisites
We need an Elasticsearch server running that we have access to.
Then we must configure the application to connect to it. This is done
in the __src/ratpack/config/EsaSampleConfig.groovy__ file. Example configuraiton:

```groovy
elasticsearch {
    cluster {
        name = "elasticsearch"
        host = 'esa.local'
        port = 9300
    }
}
```


## Starting the server
```bash
./gradlew run
```

This will start the server on port 5051. We can then send Elasticsearch queries to this address.

## Running tests
Tests can be run in Intellij, or via the command line:

```bash
./gradlew test
```

We need to have [https://github.com/Spantree/elasticsearch-appliance](https://github.com/Spantree/elasticsearch-appliance) in our
local machine, with the vm up and running:

```bash
git clone https://github.com/Spantree/elasticsearch-appliance
cd elasticsearch-appliance
vagrant up
```

_Note_: Don't clone the elasticsearch-applicance repo inside this one.

## Configuring Elsaticsearch Permissions
Restrictions can be added to our proxy via __src/ratpack/config/EsaPermissions.js__.
This is a normal javascript file and configuration is done by building an object literal with key fields. This object
literal __must__ be assigned to a variable named __base__. Example configuration:

```javascript
var base = {
  indices: {
      _default: {
          access: "allow",
          fields: ["name"],
          source_filters: ["directed_by"]
      }
  }
};
```
This creates default permissions that are applied to all indices. The following restrictions are supported:

* access => If this field is not present or empty, it prevents any query from being executed. To allow queries its 
value must be __"allow"__.
* fields => A list of strings that specify the fields that are allowed to be queried. If a query is submitted that contains
a field that is not present the proxy ignores those fields and the results will not contain them.
* source_filters => Allows us to add restrictions to source filter queries. All source filters queries are denied by default.
The following setting: __source_filters: ["directed_by"]__ is compiled to:
```
_source: {
  include: ["directed_by"]
}
```

Rules can also be applied per index:

```javascript
var base = {
  indices: {
      _default: {
          access: "allow",
          fields: ["name", "produced_by"],
          source_filters: ["directed_by"]
      },
      freebase: {
        access: "allow",
        fields: ["name"],
        source_filters: ["directed_by"]
      }
  }
};
```
A query submitted to the "freebase" index to a proxy with this setup will only return the name fields, even if other
fields are specified in the query.

## Contributing
1. Every new feature must include a test. 
2. Every new file must also include the Apache License at the beginning of the file.
3. Add at least a sentence describing what the class that is created does.
