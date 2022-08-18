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
   :style {:-fx-padding [10 0]
           :-fx-spacing 5}
   :alignment :center
   :children [{:fx/type :text-field
               :id "task-input"
               :prompt-text "Enter a task"
               :text text
               :on-text-changed {:event/type ::text-changed}}
              {:fx/type :button
               :style {:-fx-background-color :#247db8
                       :-fx-text-fill :white
                       :-fx-font-weight :bold}
               :default-button true
               :text "Add Task"
               :on-action {:event/type ::add-task}}]})

(defn gen-task-item [row task-id task-text]
  [{:fx/type :pane
    :grid-pane/column 0
    :grid-pane/row row
    ;; :grid-pane/margin is used to space the label and button because the
    ;; hgap setting in style causes the last part of the longest label to be
    ;; trimmed and ellipsized.
    :grid-pane/margin {:right 5}
    :style {:-fx-border-color :gray
            :-fx-border-style :solid
            :-fx-border-width [0 0 2 0]}
    :children [{:fx/type :label
                :text task-text}]}
   {:fx/type :button
    :grid-pane/column 1
    :grid-pane/row row
    :style {:-fx-background-color :#d82424
            :-fx-text-fill :white
            :-fx-font-weight :bold}
    :text "Delete"
    :on-action {:event/type ::delete-task
                ::task-id task-id}}])

(defn app [{:keys [typed-text tasks]}]
  {:fx/type :v-box
   :children [
              {:fx/type task-input
               :text typed-text}
              {:fx/type :h-box
               :alignment :center
               :children [{:fx/type :grid-pane
                           :style {:-fx-vgap 5}
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
  [{:keys [state fx/event]}]
  (some-> event
    .getTarget
    .getScene
    (.lookup "#task-input")
    .requestFocus)
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
