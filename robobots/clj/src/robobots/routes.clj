(ns robobots.routes
  (:require
    [reitit.ring :as ring]
    [robobots.routes.home-page :as home-page]))

(def routes
  [home-page/route])

(def handler
  (ring/ring-handler
    (ring/router routes)
    (ring/create-default-handler)))
