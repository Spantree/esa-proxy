package net.spantree.ratpack.elasticsearch

import spock.lang.Specification
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class JavascriptReaderSpec extends Specification {
    File file = new File("./src/ratpack/config/EsaPermissions.js")
    ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript")
    List<String> arrayFields = ["roles", "fields", "source_filters", "users"]

    def setup() {
        scriptEngine.eval(new FileReader(file))
    }

    def "reads javascript file"() {
        expect:
        file.text.size() > 0
    }

    def "parses javascript map as a java map"() {
        when:
        def baseObject = scriptEngine.get("base")

        then:
        ((Map) baseObject).containsKey("indices")
        baseObject.indices._default.first().access == "allow"
    }

    def "parses javascript array as a java list"() {
        when:
        def anArray = scriptEngine.get("users")

        then:
        anArray.size() == 4
        anArray.contains([username: "ringo", roles:["DRUMMER"]])
        anArray.contains([username: "george", roles:["GUITAR", "VOCALS"]])
    }

    def "parses javascript arrays nested in an object as java arrays"() {
        given:
        def anArray = scriptEngine.get("users")

        when:
        def ringo = anArray.first()

        then:
        ringo.roles == ["DRUMMER"]
    }

    def "parses an index fields"() {
        when:
        def defaultIndex = scriptEngine.get("base").indices._default

        then:
        defaultIndex.fields.size() == 1
        defaultIndex.fields.first().contains("name")

        when:
        def locationsIndex = scriptEngine.get("base").indices.locations

        then:
        locationsIndex.fields.first() == ["about", "description", "name"]
        locationsIndex.roles.first() == ["GUITAR", "DRUMMER"]
    }

}
