(ns mapmaker.views
  (:require
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
             :root {:fx/type :border-pane
                    :top {:fx/type :menu-bar
                          :menus menus}
                    :left {:fx/type :tool-bar
                           :items []}
                    :right {:fx/type :v-box
                            :children [{:fx/type :button
                                        :text "Select Tileset"
                                        :on-action {::events/type ::events/open-file
                                                    :file/kind :tileset}}
                                       {:fx/type :scroll-pane
                                        :min-viewport-width 32
                                        :pref-viewport-width 64
                                        :vbar-policy :always
                                        :v-box/vgrow :always
                                        :content (if (:tileset state)
                                                   {:fx/type :image-view
                                                    :image (str "file:" (.getAbsolutePath (get-in state [:tileset :file])))}
                                                   {:fx/type :label
                                                    :text "Select a\nTileset"})}]}
                    :bottom {:fx/type status-bar
                             :map-name (:map-name state)}}}}))
