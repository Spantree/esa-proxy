import net.spantree.esa.UserDAOModule
import net.spantree.ratpack.UserAccountDeleteHandler
import net.spantree.ratpack.UserAccountsHandler
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
        add new JacksonModule()
        add new MarkupTemplatingModule()
        add new CodaHaleMetricsModule().jvmMetrics().jmx().websocket().healthChecks()
        add new UserDAOModule(new File(configDir, "Config.groovy"))
        add new ElasticsearchModule(new File(configDir, "EsSampleConfig.groovy"))
    }

    handlers {
        get {
            render groovyTemplate("index.html", title: "My Ratpack App")
        }

        handler(":indexName/_search", registry.get(EsaProxyHandler))
        handler("esa/user_accounts", registry.get(UserAccountsHandler))
        handler("esa/user_accounts/:id", registry.get(UserAccountDeleteHandler))

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
