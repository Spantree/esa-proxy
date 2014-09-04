import net.spantree.esa.EsaPermissions
import net.spantree.ratpack.elasticsearch.ElasticsearchClientService
import net.spantree.ratpack.elasticsearch.EsaProxyHandler
import net.spantree.ratpack.elasticsearch.ElasticsearchModule
import net.spantree.ratpack.elasticsearch.users.EsaUserRepository
import ratpack.codahale.metrics.CodaHaleMetricsModule
import ratpack.codahale.metrics.HealthCheckHandler
import ratpack.codahale.metrics.MetricsWebsocketBroadcastHandler
import ratpack.groovy.markuptemplates.MarkupTemplatingModule
import ratpack.jackson.JacksonModule
import ratpack.remote.RemoteControlModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    bindings {
        String configDir = launchConfig.baseDir.file.toString() + "/config"
        File esaSampleConfig = new File(configDir, "EsaSampleConfig.groovy")
        String esaPermissionsFile = configDir + "/EsaPermissions.js"
        add new JacksonModule()
        add new MarkupTemplatingModule()
        add new CodaHaleMetricsModule().jvmMetrics().jmx().websocket().healthChecks()
        add new ElasticsearchModule(esaSampleConfig, esaPermissionsFile)
        add new RemoteControlModule()

        init { EsaUserRepository esaUserRepository, ElasticsearchClientService elasticsearchClientService, EsaPermissions esaPermissions ->
            if(!elasticsearchClientService.indexExists("esa_users")) {
                esaUserRepository.createIndex()
            }
            esaUserRepository.bulkCreate(esaPermissions.users)
            elasticsearchClientService.client.admin().indices().prepareRefresh("esa_users").execute().actionGet()
        }
    }

    handlers {
        get {
            render groovyTemplate("index.html", title: "My Ratpack App")
        }

        handler(":indexName/_search", registry.get(EsaProxyHandler))

        prefix("health") {
            get("health-check/:name?", new HealthCheckHandler())
            get("metrics-report", new MetricsWebsocketBroadcastHandler())

            get("metrics") {
                render groovyMarkupTemplate("metrics.gtpl", title: "Metrics")
            }
        }

        assets "public"
    }

}
