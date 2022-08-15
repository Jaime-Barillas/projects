(ns todo.core
  (:require
    [cljfx.api :as fx])
  (:import
    (javafx.application Platform))
  (:gen-class))

(defonce *state
  (atom {}))

;; Views

(defn task-input [params]
  {:fx/type :h-box
   :children [{:fx/type :text-field
               :prompt-text "Enter a task"}
              {:fx/type :button
               :text "Add Task"}]})

(defn root [state]
  {:fx/type :stage
   :showing :true
   :title "Todo App - Clojure(cljfx)"
   :min-width 480
   :min-height 854
   :scene {:fx/type :scene
           :root {:fx/type task-input}}})

(def renderer
  (fx/create-renderer
    :middleware (fx/wrap-map-desc assoc :fx/type root)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (Platform/setImplicitExit true)
  (fx/mount-renderer *state renderer))

(comment

  (fx/mount-renderer *state renderer)

  (fx/unmount-renderer *state renderer)

  (renderer)

)
