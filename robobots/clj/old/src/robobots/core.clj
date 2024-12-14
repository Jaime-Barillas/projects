(ns robobots.core
  (:require
    [org.httpkit.server :as server]
    [robobots.db :as db]
    [robobots.routes :as routes])
  (:gen-class))

(defn startup! []
  (db/startup!)
  (server/run-server
    routes/handler
    {:port 8080
     :legacy-return-value? false}))

(defn shutdown! [server]
  (db/shutdown!)
  (server/server-stop! server))

(defn -main [& args]
  (startup!))
