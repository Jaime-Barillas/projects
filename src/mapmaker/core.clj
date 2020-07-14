(ns mapmaker.core
  (:require
    [cljfx.api :as fx]
    [mapmaker.views :as views]
    [mapmaker.events :as events])
  (:import
    (javafx.application Platform))
  (:gen-class))

(defonce *state (atom {:title "MapMaker"
                       :tilemap {}
                       :tileset {}}))

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc (fn [state] {:fx/type views/root-view
                                               :state state}))
    :opts {:fx.opt/map-event-handler (-> events/handle-event
                                       (fx/wrap-co-effects (events/co-effects *state))
                                       (fx/wrap-effects (events/effects *state)))}))

(defn -main
  [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))

;; Convenience Functions
(comment

  (fx/mount-renderer *state renderer)

  (fx/unmount-renderer *state renderer)

  (renderer)

)
