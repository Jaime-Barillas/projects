(ns todo.core
  (:require
    [cljfx.api :as fx])
  (:import
    (javafx.application Platform))
  (:gen-class))

(defonce *state
  (atom {:typed-text ""
         :tasks []}))

;; Views

(defn task-input [params]
  {:fx/type :h-box
   :children [{:fx/type :text-field
               :prompt-text "Enter a task"
               :on-text-changed {:event/type ::text-changed}}
              {:fx/type :button
               :text "Add Task"}]})

(defn task-item [{:keys [task-id task-text]}]
  {:fx/type :h-box
   :children [{:fx/type :label
               :text task-text}
              {:fx/type :button
               :text "Delete"
               :on-action {:event/type ::delete-task
                           ::task-id task-id}}]})

(defn root [{:keys [tasks]}]
  {:fx/type :stage
   :showing :true
   :title "Todo App - Clojure(cljfx)"
   :min-width 480
   :min-height 480
   :scene {:fx/type :scene
           :root {:fx/type :v-box
                  :children (into [{:fx/type task-input}]
                              (for [task tasks]
                               {:fx/type task-item
                                :task-id (:id task)
                                :task-text (:text task)}))}}})

;; Events

(defmulti handle-event :event/type)

(defmethod handle-event :default
  [event-map]
  (throw (ex-info (str "No event handler for event: " (:event/type event-map))
           {:event event-map})))

(defmethod handle-event ::text-changed
  [{:keys [state fx/event]}]
  {:state (assoc state :typed-text event)})

(defmethod handle-event ::delete-task
  [{:keys [state todo.core/task-id]}]
  {:state (update state :tasks
            (fn [tasks]
              (vec (remove #(= task-id (:id %)) tasks))))})

(def handler (-> handle-event
               (fx/wrap-co-effects {:state (fx/make-deref-co-effect *state)})
               (fx/wrap-effects {:state (fx/make-reset-effect *state)})))

;; Main

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)
    :opts {:fx.opt/map-event-handler handler}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))

(comment

  (fx/mount-renderer *state renderer)

  (fx/unmount-renderer *state renderer)

  (renderer)

  (do
    (reset! *state
      {
       :tasks (for [i (range 4)]
                {:id i
                 :text (str "Task #" i)})
      })
    @*state)

)
