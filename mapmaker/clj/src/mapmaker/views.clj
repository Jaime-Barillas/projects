(ns mapmaker.views
  (:require
    [cljfx.api :as fx]
    [mapmaker.events :as-alias events]
    [mapmaker.tilemap :as tilemap]))

(defn menu-bar [params]
  ;; TODO: See if there is a better way of reaching the parent Scene from a
  ;; menu/menu-item. Neither menu or menu-item classes have a property or
  ;; method to usable to reach the parent Scene.
  {:fx/type fx/ext-on-instance-lifecycle
   :on-created #(let [file-menu (first (.getMenus %))
                      export-menus (.getItems (nth (.getItems file-menu) 3))
                      save-png (first export-menus)]
                  (.setUserData save-png %))
   :desc {:fx/type :menu-bar
          :menus [{:fx/type :menu
                   :text "File"
                   :items [{:fx/type :menu-item
                            :text "New"}
                           {:fx/type :menu-item
                            :text "Open..."}
                           {:fx/type :menu-item
                            :text "Save"}
                           {:fx/type :menu
                            :text "Export As"
                            :items [{:fx/type :menu-item
                                     :on-action {::events/type ::events/save-tilemap
                                                 :filetype "png"}
                                     :text "PNG"}]}]}
                  {:fx/type :menu
                   :text "Help"
                   :items [{:fx/type :menu-item
                            :text "About"}]}]}})

(defn status-bar [{:keys [map-name]}]
  {:fx/type :h-box
   :alignment :center-left
   :spacing 5
   :children [{:fx/type :label
               :text (or map-name "No Active Map")}]})

(defn two-column-grid-pane [{:keys [children]}]
  {:fx/type :grid-pane
   :hgap 5
   :vgap 5
   :children (map-indexed (fn [i child]
                            (let [row (quot i 2)
                                  column (mod i 2)]
                              (merge {:grid-pane/row row
                                      :grid-pane/column column
                                      :grid-pane/halignment (if (even? column)
                                                              :right
                                                              :left)}
                                     child)))
                          children)})

(defonce number-filter
  (fn number-filter [change]
    (when (re-matches #"\d*" (.getControlNewText change)) change)))

(defn number-text-field [{:keys [value]}]
  {:fx/type :text-field
   :text-formatter {:fx/type :text-formatter
                    :filter number-filter
                    :value-converter :long
                    :value value}})

(defn select-tileset-control [{:keys [show-dialog]}]
  {:fx/type fx/ext-let-refs
   :refs {::tileset-dialog {:fx/type :dialog
                            :title "Tileset Properties"
                            :showing show-dialog
                            :on-hidden {::events/type ::events/on-tileset-dialog-hidden}
                            :dialog-pane {:fx/type :dialog-pane
                                          :content {:fx/type two-column-grid-pane
                                                    :children [{:fx/type :label
                                                                :text "Tiles Wide"}
                                                               {:fx/type number-text-field
                                                                :value 16}
                                                               {:fx/type :label
                                                                :text "Tiles High"}
                                                               {:fx/type number-text-field
                                                                :value 16}
                                                               {:fx/type :label
                                                                :text "Tile Width"}
                                                               {:fx/type number-text-field
                                                                :value 32}
                                                               {:fx/type :label
                                                                :text "Tile Height"}
                                                               {:fx/type number-text-field
                                                                :value 32}]}
                                          :button-types [:ok]}}}
   :desc {:fx/type :button
          :text "Select Tileset"
          :on-action {::events/type ::events/open-file
                      :file/kind :tileset}}})

(defn tile-list [{:keys [tileset hovered-tile selected-tile]}]
  {:fx/type :tile-pane
   :hgap 2
   :vgap 2
   :pref-columns 3;(:width tileset)
   :children (for [y (range (:height tileset))
                   x (range (:width tileset))
                   :let [i (+ x (* y (:width tileset)))]]
               (merge (when (or (= i selected-tile) (= i hovered-tile))
                        {:effect {:fx/type :blend
                                  :mode :multiply
                                  :opacity 0.5
                                  :bottom-input {:fx/type :color-adjust
                                                 :saturation -1}
                                  :top-input {:fx/type :color-input
                                              :x 0
                                              :y 0
                                              :width (:tile-width tileset)
                                              :height (:tile-height tileset)
                                              :paint (if (= i selected-tile) :red :blue)}}})
                      {:fx/type :image-view
                       :image (:image tileset)
                       ;; FIXME: Below events are only triggered on _non-transparent_
                       ;; portions of the image. They are **NOT** fired upon mousing
                       ;; over transparent parts of the image.
                       :on-mouse-entered {::events/type ::events/set-hovered-tile}
                       :on-mouse-exited {::events/type ::events/reset-hovered-tile}
                       :on-mouse-clicked {::events/type ::events/set-selected-tile}
                       :viewport {:min-x (* x (:tile-width tileset))
                                  :min-y (* y (:tile-height tileset))
                                  :width (:tile-width tileset)
                                  :height (:tile-height tileset)}}))})

(defn tileset-pane [{:keys [tileset show-dialog hovered-tile selected-tile]}]
  {:fx/type :v-box
   :children [{:fx/type select-tileset-control
               :show-dialog show-dialog}
              {:fx/type :scroll-pane
               :min-viewport-width (:tile-width tileset 100)
               :vbar-policy :always
               :pref-height 0
               :v-box/vgrow :always
               :content (if (contains? tileset :width)
                          {:fx/type tile-list
                           :tileset tileset
                           :hovered-tile hovered-tile
                           :selected-tile selected-tile}
                          {:fx/type :label
                           :text "Select a\nTileset"})}]})

(defn root-view [{:keys [state]}]
  {:fx/type :stage
   :showing true
   :title (:title state)
   :icons [{:fx/type :image
            :url "images/icon.png"}]
   :min-width 800
   :min-height 600
   :scene {:fx/type :scene
           :root {:fx/type fx/ext-on-instance-lifecycle
                  ;; TODO: This will resize the canvas, but it does not cause the app
                  ;; state to update and thus not cause the canvas to repaint.
                  ;; This means the user has to perform some action that updates the app
                  ;; state to see the tilemap repainted at the new size. Consider using
                  ;; addListener on the canvas' width and height properties to issue a
                  ;; redraw (via event or the fn in the tilemap ns?)
                  :on-created #(let [canvas (.lookup % "#canvas")
                                     pane (.getParent canvas)]
                                 (.bind (.widthProperty canvas) (.widthProperty pane))
                                 (.bind (.heightProperty canvas) (.heightProperty pane)))
                  :desc {:fx/type :border-pane
                         :top {:fx/type menu-bar}
                         :left {:fx/type :tool-bar
                                :items []}
                         :right {:fx/type tileset-pane
                                 :tileset (:tileset state)
                                 :show-dialog (:show-tileset-dialog state)
                                 :hovered-tile (:hovered-tile state)
                                 :selected-tile (:selected-tile state)}
                         :bottom {:fx/type status-bar
                                  :map-name (:map-name state)}
                         :center {:fx/type :pane
                                  :children [{:fx/type :canvas
                                              :id "canvas"
                                              :on-mouse-moved {::events/type ::events/mouse-moved}
                                              :on-mouse-clicked {::events/type ::events/set-canvas-tile}
                                              :draw #(tilemap/draw % state)}]}}}}})
