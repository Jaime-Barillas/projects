(ns robobots.core
  (:require
    [next.jdbc :as jdbc]
    [org.httpkit.server :as server]
    [reitit.ring :as ring])
  (:gen-class))

(def ds (jdbc/get-datasource "jdbc:sqlite:robobots.db"))

(defn- handler [req]
  (let [table-name (get-in req [:path-params :table-name])
        ;; CAREFUL OF SQL INJECTIONS --------------vvvvvvvvvv
        r (jdbc/execute! ds [(str "SELECT * FROM " table-name)])]
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (with-out-str (clojure.pprint/pprint r))}))

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

(defn shutdown! [server]
  (server/server-stop! server))
