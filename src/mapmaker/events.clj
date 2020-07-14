(ns mapmaker.events
  (:require [cljfx.api :as fx])
  (:import
    (javafx.scene.image Image)
    (javafx.stage FileChooser FileChooser$ExtensionFilter)))

(defn co-effects [*state]
  {:state (fx/make-deref-co-effect *state)})

(defn effects [*state]
  {:state (fx/make-reset-effect *state)
   :dispatch fx/dispatch-effect})

;;;;;;;;;;;;;;;;;;;;;;
;;; Helper Functions
;;;;;;;;;;;;;;;;;;;;;;

(defn- ->ExtensionFilters [filters]
  (letfn [(create-filter [desc exts]
             (FileChooser$ExtensionFilter. desc exts))]
    (into-array FileChooser$ExtensionFilter
      (reduce-kv
        (fn [filters desc exts] (conj filters (create-filter desc exts)))
        []
        filters))))

;;;;;;;;;;;;;;;;;;;;;;
;;; Event Handlers
;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle-event ::type)

(defmethod handle-event :default [event-map]
  (let [event (:event/type event-map)
        data (select-keys (meta #'handle-event) [:ns :name])]
    (throw (ex-info (str "No event handler for event: " event)
                    {:event-handler (str (:ns data) "/" (:name data))
                     :event event-map}))))

(defmethod handle-event ::open-file [{:keys [fx/event state file/kind]}]
  (let [window (.. event (getTarget) (getScene) (getWindow))
        chooser (doto (FileChooser.)
                  (.setTitle (str "Select a " (name kind))))
        ext-filter (.getExtensionFilters chooser)]
    (.addAll ext-filter (->ExtensionFilters {"Image Files" ["*.png" "*.jpg" "*.gif"]
                                             "All Files" ["*.*"]}))
    (when-let [file (.showOpenDialog chooser window)]
      {:state (assoc state :tileset {:image (Image. (str "file:" (.getAbsolutePath file)))
                                     :width 16
                                     :height 12
                                     :tile-width 32
                                     :tile-height 32})})))

(defmethod handle-event ::gen-tileset [{:keys [state file]}]
  {:state (assoc state :tileset {:image (Image. (str "file:" (.getAbsolutePath file)))
                                 :width 3
                                 :height 3
                                 :tile-width 16
                                 :tile-height 16})})
