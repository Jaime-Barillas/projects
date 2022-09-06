(ns robobots.core
  (:require
    [org.httpkit.server :as server]
    [reitit.ring :as ring])
  (:gen-class))

(defn- handler [req]
  (let [params (:path-params req)]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (str "You requested the contents of the " (:table-name params) " table.")}))

(def app
  (ring/ring-handler
    (ring/router
      ["/table/:table-name" handler])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (server/run-server
    app
    {:port 8080
     :legacy-return-value? false}))
