(ns software.justenough.itsallgravie.home
  (:require [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.typography :refer [typography]]
            [software.justenough.itsallgravie.api-key :as api-key]))

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
   [api-key/input]])
