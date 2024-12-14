(ns user)

(defn hello
  ([] (hello "World"))
  ([msg] (str "Hello, " msg "!")))

(comment
  (hello "Jaime"))
