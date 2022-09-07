(ns robobots.routes.home-page
  (:require
    [robobots.db :as db]
    [robobots.html :as html]
    [robobots.routes :as-alias routes]))

(defn home-page [req]
  (let [table-name (get-in req [:path-params :table-name])
        table (db/select-all-from table-name)]
    {:status 200
     :headers {"Content-Type" "text/html"}
     ;; hiccup.util/escape-html table-name below!!!
     :body (html/html table-name
             [:h1 table-name]
             (html/table table))}))

(def route
  ["/:table-name" {:name ::routes/home-page
                   :get home-page}])
