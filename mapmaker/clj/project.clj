(defproject mapmaker "0.1.0-SNAPSHOT"
  :description "A program to help you make tile based 2D maps."
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/mit"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [cljfx "1.7.21"]
                 [org.openjfx/javafx-swing "17.0.2"]]
  :main ^:skip-aot mapmaker.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :injections [(javafx.application.Platform/exit)]}})
