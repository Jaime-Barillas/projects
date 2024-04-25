(defproject robobots "0.1.0-SNAPSHOT"
  :description "A website for building Robo-Robots!"
  :url "http://example.com/FIXME"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/mit"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [http-kit/http-kit "2.6.0"]
                 [metosin/reitit-ring "0.5.18"]
                 [org.xerial/sqlite-jdbc "3.39.2.1"]
                 [com.github.seancorfield/next.jdbc "1.2.796"]
                 [hiccup/hiccup "1.0.5"]]
  :main ^:skip-aot robobots.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
