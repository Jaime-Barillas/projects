(ns mapmaker.events
  (:import
    (javafx.stage FileChooser FileChooser$ExtensionFilter)))

(defn deref-co-effect
  "Derefs the provided state atom for further event processing."
  [*state]
  (deref *state))

(defn reset-effect!
  "Resets the provided state atom with the value returned from running the event."
  [*state event-result _]
  (reset! *state event-result))

(defn co-effects [*state]
  {:state (fn [] (deref-co-effect *state))})

(defn effects [*state]
  {:state (fn [event-result dispatch-fn!]
            (reset-effect! *state event-result dispatch-fn!))})

(defmulti handle-event :event/type)

(defn ->ExtensionFilters [filters]
  (letfn [(create-filter [desc exts]
             (FileChooser$ExtensionFilter. desc exts))]
    (into-array FileChooser$ExtensionFilter
      (reduce-kv
        (fn [acc k v] (conj acc (create-filter k v)))
        []
        filters))))

(defmethod handle-event ::open-file [{:keys [fx/event state file/kind]}]
  (let [window (.. event (getTarget) (getScene) (getWindow))
        chooser (doto (FileChooser.)
                  (.setTitle (str "Select a " (name kind))))
        ext-filter (.getExtensionFilters chooser)]
    (.addAll ext-filter (->ExtensionFilters {"Image Files" ["*.png" "*.jpg" "*.gif"]
                                             "All Files" ["*.*"]}))
    (when-let [file (.showOpenDialog chooser window)]
      {:state (assoc state :tileset {:file file})})))

(defmethod handle-event :default [event-map]
  (let [event (:event/type event-map)
        data (select-keys (meta #'handle-event) [:ns :name])]
    (throw (ex-info (str "No event handler for event: " event)
                    {:event-handler (str (:ns data) "/" (:name data))
                     :event event}))))
