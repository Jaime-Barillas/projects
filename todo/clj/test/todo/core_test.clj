(ns todo.core-test
  (:require [clojure.test :refer :all]
            [todo.core :as todo :refer [handle-event]]))

(deftest add-task
  (testing "A new task"
    (let [before {:state {:typed-text "A new task"
                          :tasks []}}
          after {:state {:typed-text ""
                         :tasks [{:id 0 :text "A new task"}]}}]
      (is (= after
            (handle-event (merge before {:event/type ::todo/add-task}))))))

  (testing "Empty task text"
    (let [before {:state {:typed-text ""
                          :tasks []}}]
      (is (= before
            (handle-event (merge before {:event/type ::todo/add-task})))))))

(deftest delete-task
  (let [before {:state {:tasks [{:id 0 :text "A task"}
                                {:id 1 :text "A second task"}]}}
        after {:state {:tasks [{:id 0 :text "A task"}]}}]
    (testing "Deleting a task"
      (is (= after
            (handle-event (merge before {:event/type ::todo/delete-task
                                         ::todo/task-id 1})))))

    (testing "Deleting non-existant task"
      (is (= before
            (handle-event (merge before {:event/type ::todo/delete-task
                                         ::todo/task-id -1})))))))
