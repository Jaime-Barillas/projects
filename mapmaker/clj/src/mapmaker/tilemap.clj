(ns mapmaker.tilemap
  (:import
    (javafx.scene.canvas Canvas)
    (javafx.scene.paint Color)))

(set! *warn-on-reflection* true)

;; tileset:
;; :image javafx.scene.image.Image
;; :width num
;; :height num
;; :tile-width num
;; :tile-height num

(defn draw [^Canvas canvas state]
  (let [gc (.getGraphicsContext2D canvas)]
    (.setFill gc Color/YELLOW)
    (.fillRect gc 0 0 150 100)
    (.setFill gc Color/BLACK)
    (.fillText gc (str "w:" (.getWidth canvas) " - h:" (.getHeight canvas)) 0 20)
    (.fillText gc (str "selected: " (:selected-tile state) " - "
                    "hovered: " (:hovered-tile state))
      0 40)))
