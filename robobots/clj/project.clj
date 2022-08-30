(defproject robobots "0.1.0-SNAPSHOT"
  :description "A website for building Robo-Robots!"
  :url "http://example.com/FIXME"
  :license {:name "ISC"
            :url "https://opensource.org/licenses/ISC"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :main ^:skip-aot robobots.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
