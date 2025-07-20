(defproject cca-mcp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.11.1"]
                 [cheshire "5.12.0"]
                 [org.slf4j/slf4j-simple "2.0.9"]]
  :repositories [["mcp-releases" "https://repo.modelcontextprotocol.io/releases"]]
  :resource-paths ["src/main/resources"]
  :main ^:skip-aot cca-mcp.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
