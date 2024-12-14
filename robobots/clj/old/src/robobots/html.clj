(ns robobots.html
  (:require
    [hiccup.page :as hic-page]))

(defn html-head [title]
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
   [:title title]])

(defn html [title & body]
  (hic-page/html5 {:lang "en"}
    (html-head title)
    [:body body]))

(defn table-row [row]
  [:tr
   (for [[_ v] row]
     [:td v])])

(defn table [rows]
  (let [headers (keys (first rows))]
    [:table
     [:tr
      (for [header headers]
        [:th header])]
     (map table-row rows)]))
