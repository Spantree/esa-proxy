import net.spantree.ratpack.elasticsearch.EsaProxyHandler
import net.spantree.ratpack.elasticsearch.ElasticsearchModule
import ratpack.codahale.metrics.CodaHaleMetricsModule
import ratpack.codahale.metrics.HealthCheckHandler
import ratpack.codahale.metrics.MetricsWebsocketBroadcastHandler
import ratpack.groovy.markuptemplates.MarkupTemplatingModule
import ratpack.jackson.JacksonModule

import static ratpack.groovy.Groovy.groovyMarkupTemplate
import static ratpack.groovy.Groovy.groovyTemplate
import static ratpack.groovy.Groovy.ratpack

ratpack {
    bindings {
        String configDir = launchConfig.baseDir.file.toString() + "/config"
        File esaSampleConfig = new File(configDir, "EsaSampleConfig.groovy")
        String esaPermissions = configDir + "/EsaPermissions.js"
        add new JacksonModule()
        add new MarkupTemplatingModule()
        add new CodaHaleMetricsModule().jvmMetrics().jmx().websocket().healthChecks()
        add new ElasticsearchModule(esaSampleConfig, esaPermissions)
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
