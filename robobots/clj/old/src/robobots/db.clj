(ns robobots.db
  (:require
    [next.jdbc :as jdbc]))

(def ds (atom nil))

(defn startup! []
  (reset! ds (jdbc/get-datasource "jdbc:sqlite:robobots.db")))

(defn shutdown! []
  (reset! ds nil))

(defn select-all-from [table-name]
  ;; CAREFUL OF SQL INJECTIONS -------------vvvvvvvvvv
  (jdbc/execute! @ds [(str "SELECT * FROM " table-name ";")]))
