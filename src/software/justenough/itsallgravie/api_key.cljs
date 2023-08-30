(ns software.justenough.itsallgravie.api-key
  "This contains the various functions for handling the api-key."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            [software.justenough.itsallgravie.utils :as utils]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [clojure.string :as str]))

(rf/reg-event-db
 ::change
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
     "API Key: "
     [:input {:type      "password"
              :style     {:border "1px solid #CCC"}
              :value     api-key
              :on-change emit}]]))

