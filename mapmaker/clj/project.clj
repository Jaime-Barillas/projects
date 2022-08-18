(defproject mapmaker "0.1.0-SNAPSHOT"
  :description "A program to help you create tiled maps."
  :url "http://example.com/FIXME"
  :license {:name "ISC"
            :url "https://opensource.org/licenses/ISC"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cljfx "1.7.2"]]
  :main ^:skip-aot mapmaker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
