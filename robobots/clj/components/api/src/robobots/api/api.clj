(ns robobots.api.api
  (:require
   [reitit.ring :as ring]
   [reitit.ring.middleware.parameters :as parameters]
   [reitit.ring.middleware.multipart :as multipart]
   [robobots.api.core :as core]))

(def app
  (ring/ring-handler
    (ring/router
      [["/api"
        ["/parts"            {:get core/handler}]
        ["/part/:id"         {:get core/handler}]

        ["/user/robobots"    {:get core/handler
                              :post core/handler}]
        ["/user/robobot/:id" {:get core/handler
                              :patch core/handler
                              :delete core/handler}]
        ["/user/tournaments" {:get core/handler}]

        ["/tournaments"      {:get core/handler}]
        ["/tournament/:id"   {:get core/handler
                              :put core/handler
                              :delete core/handler}]]

       ["/auth"
        ["/signup" {:post core/handler}]
        ["/login"  {:post core/handler}]
        ["/logout" {:get core/handler}]]]

      {:data
       {:middleware
        [parameters/parameters-middleware
         multipart/multipart-middleware]}})))
