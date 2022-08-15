(defproject todo "0.1.0-SNAPSHOT"
  :description "A simple desktop todo app written in Clojure."
  :url "http://example.com/FIXME"
  :license {:name "ISC"
            :url "https://opensource.org/licenses/ISC"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cljfx/cljfx "1.7.21"]]
  :main ^:skip-aot todo.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dcljfx.skip-javafx-initialization=true"]}})
