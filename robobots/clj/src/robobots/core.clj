(ns robobots.core
  (:require
    [hiccup.core :as hiccup]
    [hiccup.page :as page]
    [next.jdbc :as jdbc]
    [org.httpkit.server :as server]
    [reitit.ring :as ring])
  (:gen-class))

(def ds (jdbc/get-datasource "jdbc:sqlite:robobots.db"))

(defn html-head [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:title title]])

(defn html [title & body]
  (page/html5 {:lang "en"}
    (html-head title)
    [:body body]))

(defn db-row->html-tr [row]
  [:tr
   (for [[_ v] row]
     [:td v])])

(defn db-table->html-table [db-table]
  (let [headers (keys (first db-table))]
    [:table
     [:tr
      (for [header headers]
        [:th header])]
     (map db-row->html-tr db-table)]))

(defn- handler [req]
  (let [table-name (get-in req [:path-params :table-name])
        ;; CAREFUL OF SQL INJECTIONS --------------vvvvvvvvvv
        r (jdbc/execute! ds [(str "SELECT * FROM " table-name)])]
    {:status 200
     :headers {"Content-Type" "text/html"}
     ;; hiccup.util/escape-html table-name below!!!
     :body (html table-name
             [:h1 table-name]
             (db-table->html-table r))}))

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

(comment
  (hiccup/html [:html {:lang "en"}
                [:head
                 [:title "RoboBots"]]
                [:body
                 [:p "hello!"]]])
  (page/html5 {:lang "en"} [:p "test"])
  )
