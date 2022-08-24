(ns mapmaker.events
  (:require [cljfx.api :as fx])
  (:import
    (javafx.scene.image Image)
    (javafx.stage FileChooser FileChooser$ExtensionFilter)))

(defn co-effects [*state]
  {:state (fx/make-deref-co-effect *state)})

(defn effects [*state]
  {:state (fx/make-reset-effect *state)
   :dispatch fx/dispatch-effect})

;;;;;;;;;;;;;;;;;;;;;;
;;; Helper Functions
;;;;;;;;;;;;;;;;;;;;;;

(defn- ->ExtensionFilters [filters]
  (letfn [(create-filter [desc exts]
             (FileChooser$ExtensionFilter. desc exts))]
    (into-array FileChooser$ExtensionFilter
      (reduce-kv
        (fn [filters desc exts] (conj filters (create-filter desc exts)))
        []
        filters))))

;;;;;;;;;;;;;;;;;;;;;;
;;; Event Handlers
;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle-event ::type)

(defmethod handle-event :default [event-map]
  (let [event (:event/type event-map)
        data (select-keys (meta #'handle-event) [:ns :name])]
    (throw (ex-info (str "No event handler for event: " event)
                    {:event-handler (str (:ns data) "/" (:name data))
                     :event event-map}))))

(defmethod handle-event ::open-file [{:keys [fx/event state file/kind]}]
  (let [window (.. event (getTarget) (getScene) (getWindow))
        chooser (doto (FileChooser.)
                  (.setTitle (str "Select a " (name kind))))
        ext-filter (.getExtensionFilters chooser)]
    (.addAll ext-filter (->ExtensionFilters {"Image Files" ["*.png" "*.jpg" "*.gif"]
                                             "All Files" ["*.*"]}))
    (when-let [file (.showOpenDialog chooser window)]
      {:state (assoc state
                     :tileset {:image (->> file
                                           (.getAbsolutePath)
                                           (str "file:")
                                           (Image.))}
                     :show-tileset-dialog true)})))

(defmethod handle-event ::on-tileset-dialog-hidden [{:keys [state fx/event]}]
  (let [content (.. event (getTarget) (getDialogPane) (getContent) (getChildren))
        text-fields (map #(.getValue (.getTextFormatter %))
                         (take-nth 2 (drop 1 content)))]
    {:state (assoc state :show-tileset-dialog false)
     :dispatch {::type ::gen-tileset
                :tileset-attributes text-fields}}))

(defmethod handle-event ::gen-tileset [{:keys [state tileset-attributes]}]
  {:state (-> state
              (assoc-in [:tileset :width] (nth tileset-attributes 0))
              (assoc-in [:tileset :height] (nth tileset-attributes 1))
              (assoc-in [:tileset :tile-width] (nth tileset-attributes 2))
              (assoc-in [:tileset :tile-height] (nth tileset-attributes 3)))
   :dispatch {::type ::reset-selected-tile}})

(defmethod handle-event ::set-hovered-tile [{:keys [state fx/event]}]
  (let [tile (.getTarget event)
        tiles (.. tile (getParent) (getChildren))]
    {:state (assoc state :hovered-tile (.indexOf tiles tile))}))

(defmethod handle-event ::reset-hovered-tile [{:keys [state]}]
  {:state (assoc state :hovered-tile nil)})

(defmethod handle-event ::set-selected-tile [{:keys [state fx/event]}]
  (let [tile (.getTarget event)
        tiles (.. tile (getParent) (getChildren))]
    {:state (assoc state :selected-tile (.indexOf tiles tile))}))

(defmethod handle-event ::reset-selected-tile [{:keys [state]}]
  {:state (assoc state :selected-tile nil)})

(defmethod handle-event ::mouse-moved [{:keys [state fx/event]}]
  ;; TODO: Better handle uninitialized portions of app state.
  (let [mouse-x (.getX event)
        mouse-y (.getY event)
        tile-width (or (get-in state [:tileset :tile-width]) 1)
        tile-height (or (get-in state [:tileset :tile-height]) 1)
        [prev-x prev-y] (get-in state [:tilemap :hovered-tile])
        new-x (quot mouse-x tile-width)
        new-y (quot mouse-y tile-height)]
    (if (or (not= prev-x new-x) (not= prev-y new-y))
      {:state (assoc-in state [:tilemap :hovered-tile] [new-x new-y])}
      {:state state})))

(defmethod handle-event ::set-canvas-tile [{:keys [state fx/event]}]
  (let [mouse-x (.getX event)
        mouse-y (.getY event)
        tile-width (or (get-in state [:tileset :tile-width]) 1)
        tile-height (or (get-in state [:tileset :tile-height]) 1)
        new-x (quot mouse-x tile-width)
        new-y (quot mouse-y tile-height)
        selected-tile (:selected-tile state)]
    {:state (assoc-in state [:tilemap [new-x new-y]] selected-tile)}))
