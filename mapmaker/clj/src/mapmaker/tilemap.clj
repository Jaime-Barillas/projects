(ns mapmaker.tilemap
  (:import
    (javafx.scene.canvas Canvas)
    (javafx.scene.paint Color)))

(set! *warn-on-reflection* true)

(defn draw [^Canvas canvas state]
  (let [gc (.getGraphicsContext2D canvas)
        tilemap (:tilemap state)
        tileset (get-in state [:tileset :image])
        [hovered-x hovered-y] (get-in state [:tilemap :hovered-tile])
        tile-width (get-in state [:tileset :tile-width])
        tile-height (get-in state [:tileset :tile-height])]
    (.clearRect gc 0 0 (.getWidth canvas) (.getHeight canvas))

    ;; Draw entire tilemap.
    (.setFill gc Color/BLUE)
    (let [tiles (filter #(vector? (first %)) tilemap)
          tiles-wide (get-in state [:tileset :width])
          tiles-high (get-in state [:tileset :height])]

      ;; TODO: Better nil checks.
      (when (seq tiles)
        (doseq [[[x y] selected-tile] tiles]
          (when (and x y selected-tile)
            (.setImageSmoothing gc false)
            (.drawImage gc tileset
              (* (mod selected-tile tiles-wide) tile-width)
              (* (quot selected-tile tiles-wide) tile-height)
              tile-width
              tile-height
              (* x tile-width)
              (* y tile-height)
              tile-width
              tile-height)))))

    ;; Draw hovered tile overlay.
    (.setFill gc (Color. 1 1 0 0.25))
    (when (and hovered-x hovered-y)
      (.fillRect gc
        (* hovered-x tile-width)
        (* hovered-y tile-height)
        tile-width
        tile-height))))
