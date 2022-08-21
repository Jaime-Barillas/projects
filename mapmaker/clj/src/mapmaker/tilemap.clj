(ns mapmaker.tilemap
  (:import
    (javafx.scene.canvas Canvas)
    (javafx.scene.paint Color)))

(set! *warn-on-reflection* true)

(defn draw [^Canvas canvas state]
  (let [gc (.getGraphicsContext2D canvas)
        [hovered-x hovered-y] (get-in state [:tilemap :hovered-tile])
        tile-width (get-in state [:tileset :tile-width])
        tile-height (get-in state [:tileset :tile-height])]
    (.clearRect gc 0 0 (.getWidth canvas) (.getHeight canvas))
    (.setFill gc (Color. 1 1 0 0.25))
    (when (and hovered-x hovered-y)
      (.fillRect gc
        (* hovered-x tile-width)
        (* hovered-y tile-height)
        tile-width
        tile-height))))
