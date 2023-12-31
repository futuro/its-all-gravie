(ns software.justenough.itsallgravie.game
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
            [reagent-mui.material.card :rename {card mui-card}]
            [reagent-mui.material.card-actions :refer [card-actions]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.card-media :refer [card-media]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.typography :refer [typography]]
            [software.justenough.itsallgravie.cart :as cart]))

(rf/reg-sub
 ::game
 (fn [db [_ game-ref]]
   (get-in db game-ref)))

(rf/reg-sub
 ::borrowed-games
 (fn [db _]
   (:borrowed-games db)))

(rf/reg-event-db
 ::return-game
 (fn [db [_ game-ref]]
   (update db :borrowed-games disj game-ref)))

(defn card
  [game-ref]
  (let [game              @(rf/subscribe [::game game-ref])
        current-cart      @(rf/subscribe [::cart/current-cart])
        borrowed-games    @(rf/subscribe [::borrowed-games])
        title             (:name game)
        thumbnail-url     (get-in game [:image :thumb_url])
        add-to-cart!      (fn [] (rf/dispatch [::cart/add-to-cart game-ref]))
        remove-from-cart! (fn [] (rf/dispatch [::cart/remove-from-cart game-ref]))
        return-game!      (fn [] (rf/dispatch [::return-game game-ref]))]
    [grid2 {:xs 2
            :sx {:flexGrow 1}}
     [mui-card
      [card-media {:component :img
                   :image     thumbnail-url}]
      [card-content
       [typography {:gutterBottom true
                    :variant      :h5
                    :component    :div}
        title]]
      [card-actions
       (cond
         (contains? borrowed-games game-ref)
         [button {:size     :small
                  :on-click return-game!}
          "Return game"]

         (contains? current-cart game-ref)
         [button {:size     :small
                  :on-click remove-from-cart!}
          "Remove from cart!"]

         true
         [button {:size     :small
                  :color    :primary
                  :on-click add-to-cart!}
          "Add to cart!"])]]]))

(defn grid
  [game-refs]
  [grid2 {:container true
          :spacing 4
          :flex-wrap :wrap
          :sx {:flexGrow 1}}
   (for [game-ref game-refs]
     ^{:key game-ref} [card game-ref])])
