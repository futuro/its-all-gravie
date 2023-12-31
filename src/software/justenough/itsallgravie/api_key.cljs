(ns software.justenough.itsallgravie.api-key
  "This contains the various functions for handling the api-key."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [reagent-mui.material.text-field :refer [text-field]]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            [software.justenough.itsallgravie.utils :as utils]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [clojure.string :as str]
            [reagent-mui.material.typography :refer [typography]]
            [software.justenough.itsallgravie.db :as db]))

(rf/reg-event-db
 ::change
 [db/persist-db-intercepter]
 (fn [db [_ new-api-key]]
   (assoc db :api-key (str/trim new-api-key))))

(rf/reg-sub
 ::value
 (fn [db _]
   (:api-key db)))

(defn input
  []
  (let [emit    (fn [e] (rf/dispatch [::change (utils/event->value e)]))
        api-key @(rf/subscribe [::value])]
    [:div
     {:style {:display :flex
              :flex-direction :column}}
     [typography {:variant :body1
                  :mb "5px"}
      "Enter in your Giant Bomb API key to start borrowing games!"]
     [text-field {:label "API Key"
                  :variant "outlined"
                  :value api-key
                  :type :password
                  :on-change emit}]]))

