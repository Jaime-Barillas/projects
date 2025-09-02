(ns robobots.server.core
  (:require
    [org.httpkit.server :as server]
    [robobots.api.api :as api]))

(defn -main [& args]
  (println "Listening on port 8080...")
  (server/run-server api/app {:port 8080}))
