(ns mapmaker.views
  (:require
    [cljfx.api :as fx]
    [mapmaker.events :as events])
  (:import
    (javafx.stage Screen)))

(def menus
  [{:fx/type :menu
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
                      :text "PNG"}
                     {:fx/type :menu-item
                      :text "JPG"}]}]}
   {:fx/type :menu
    :text "Help"
    :items [{:fx/type :menu-item
             :text "About"}]}])

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

(defn number-filter [change]
  (when (re-matches #"\d*" (.getControlNewText change)) change))

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

(defn tile-list [{:keys [tileset]}]
  {:fx/type :tile-pane
   :hgap 2
   :vgap 2
   :pref-columns 3;(:width tileset)
   :children (for [y (range (:height tileset))
                   x (range (:width tileset))]
               {:fx/type :image-view
                :image (:image tileset)
                :viewport {:min-x (* x (:tile-width tileset))
                           :min-y (* y (:tile-height tileset))
                           :width (:tile-width tileset)
                           :height (:tile-height tileset)}})})

(defn tileset-pane [{:keys [tileset show-dialog]}]
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
                           :tileset tileset}
                          {:fx/type :label
                           :text "Select a\nTileset"})}]})

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
     :min-width 800
     :min-height 600
     :scene {:fx/type :scene
             :root {:fx/type :border-pane
                    :top {:fx/type :menu-bar
                          :menus menus}
                    :left {:fx/type :tool-bar
                           :items []}
                    :right {:fx/type tileset-pane
                            :tileset (:tileset state)
                            :show-dialog (:show-tileset-dialog state)}
                    :bottom {:fx/type status-bar
                             :map-name (:map-name state)}}}}))
