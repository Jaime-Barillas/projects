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

(defn task-input [{:keys [text]}]
  {:fx/type :h-box
   :children [{:fx/type :text-field
               :prompt-text "Enter a task"
               :text text
               :on-text-changed {:event/type ::text-changed}}
              {:fx/type :button
               :text "Add Task"
               :on-action {:event/type ::add-task}}]})

(defn gen-task-item [row task-id task-text]
  [{:fx/type :label
    :grid-pane/column 0
    :grid-pane/row row
    :text task-text}
   {:fx/type :button
    :grid-pane/column 1
    :grid-pane/row row
    :text "Delete"
    :on-action {:event/type ::delete-task
                ::task-id task-id}}])

(defn app [{:keys [typed-text tasks]}]
  {:fx/type :v-box
   :children [{:fx/type :h-box
               :alignment :center
               :children [{:fx/type task-input
                           :text typed-text}]}
              {:fx/type :h-box
               :alignment :center
               :children [{:fx/type :grid-pane
                           :column-constraints [{:fx/type :column-constraints
                                                 :percent-width 70}
                                                {:fx/type :column-constraints
                                                 :percent-width 30}]
                           :children (apply concat
                                       (map-indexed
                                         (fn [i task]
                                           (gen-task-item i (:id task) (:text task)))
                                         tasks))}]}]})

(defn root [state]
  {:fx/type :stage
   :showing :true
   :title "Todo App - Clojure(cljfx)"
   :min-width 480
   :min-height 480
   :scene {:fx/type :scene
           :root (merge state {:fx/type app})}})

;; Events

(defmulti handle-event :event/type)

(defmethod handle-event :default
  [event-map]
  (throw (ex-info (str "No event handler for event: " (:event/type event-map))
           {:event event-map})))

(defmethod handle-event ::text-changed
  [{:keys [state fx/event]}]
  {:state (assoc state :typed-text event)})

(defmethod handle-event ::add-task
  [{:keys [state]}]
  (let [last-task (-> state :tasks peek)
        next-id (if last-task
                  (inc (:id last-task))
                  0)
        task-text (:typed-text state)]
    (if-not (empty? task-text)
      {:state (-> state
                (update :tasks conj {:id next-id :text task-text})
                (assoc :typed-text ""))}
      {:state state})))

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
