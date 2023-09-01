(ns software.justenough.itsallgravie.home
  (:require [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.divider :refer [divider]]
            [reagent-mui.material.typography :refer [typography]]
            [software.justenough.itsallgravie.api-key :as api-key]))

(defn rented-games
  []
  [typography
   {:variant :h3}
   "Currently Checked Out Games"])

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
   [rented-games]])
