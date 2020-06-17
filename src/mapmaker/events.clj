(ns mapmaker.events)

(defn deref-co-effect [*state]
  "Derefs the provided state atom for further event processing."
  (deref *state))

(defn reset-effect! [*state event-result _]
  "Resets the provided state atom with the value returned from running the event."
  (reset! *state event-result))

(defn co-effects [*state]
  {:state (fn [] (deref-co-effect *state))})

(defn effects [*state]
  {:state (fn [event-result dispatch-fn!]
            (reset-effect! *state event-result dispatch-fn!))})

(defmulti handle-event :event/type)

(defmethod handle-event :default [event-map]
  (let [event (:event/type event-map)
        data (select-keys (meta #'handle-event) [:ns :name])]
    (throw (ex-info (str "No event handler for event: " event)
                    {:event-handler (str (:ns data) "/" (:name data))
                     :event event}))))
