(ns software.justenough.itsallgravie.home
  (:require [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.typography :refer [typography]]
            [software.justenough.itsallgravie.api-key :as api-key]
            [re-frame.core :as rf]
            [software.justenough.itsallgravie.game :as game]))

(defn borrowed-games
  []
  (let [borrowed-games @(rf/subscribe [::game/borrowed-games])]
    [container
     [typography
      {:variant :h3}
      "Currently Checked Out Games"]
     ;; TODO:
     ;; - Tweak the sizing of the cards?
     ;; - Alter the text on each card to a "return game" text
     [game/grid borrowed-games]]))

(defn page
  []
  [container {:maxWidth "lg"
              :sx {:margin-top 4
                   :display "flex"
                   :alignItems "center"
                   :flexDirection "column"}}
   [typography
    {:variant :h3}
    "Welcome back!"]
   [api-key/input]
   [divider {:flexItem true
             :sx {:my "10px"}}]
   [borrowed-games]])
