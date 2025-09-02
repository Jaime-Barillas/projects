(ns robobots.api.core)

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (let [s (StringBuilder.)]
           (StringBuilder/.append s "<h1>Hello, World!</h1>")
           (doseq [[k v] (:query-params req)]
             (.append s "<p>")
             (.append s k)
             (.append s " = ")
             (.append s v)
             (.append s "</p>"))
           (.toString s))})
