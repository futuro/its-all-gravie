(ns software.justenough.itsallgravie.checkout
  (:require [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [software.justenough.itsallgravie.utils :as utils]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            [reagent-mui.material.unstable-grid-2 :rename {unstable-grid-2 grid2}]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.card-actions :refer [card-actions]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.card-media :refer [card-media]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.typography :refer [typography]]))

(defn page
  []
  [typography {:variant :h3}
   "Let's rent those games!"])

;; TODO:
;; - display the items in the cart, with the ability to remove them from the cart
;; - have some kind of button that does the renting
;; - maybe skip any semblance of billing? That doesn't seem relevant for the goals of the project 🤔
