(ns mapmaker.views
  (:import
    (javafx.stage Screen)))

(defn root-view [{:keys [state]}]
  (let [bounds (.getVisualBounds (Screen/getPrimary))
        x (.getMinX bounds)
        y (.getMinY bounds)]
    {:fx/type :stage
     :showing true
     :title (:title state)
     :icons [{:fx/type :image
              :url "images/icon.png"}]
     :x x
     :y y
     :min-width 480
     :min-height 360
     :scene {:fx/type :scene
             :root {:fx/type :label
                    :text "FIXME"}}}))
