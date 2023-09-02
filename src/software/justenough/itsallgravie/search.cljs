(ns software.justenough.itsallgravie.search
  (:require [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [software.justenough.itsallgravie.utils :as utils]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            [reagent-mui.material.unstable-grid-2 :rename {unstable-grid-2 grid2}]
            [reagent-mui.material.text-field :refer [text-field]]
            [reagent-mui.material.card :refer [card]]
            [reagent-mui.material.card-actions :refer [card-actions]]
            [reagent-mui.material.card-content :refer [card-content]]
            [reagent-mui.material.card-media :refer [card-media]]
            [reagent-mui.material.container :refer [container]]
            [reagent-mui.material.typography :refer [typography]]))

;; -- Events -------------------------------------------------------

(rf/reg-event-db
 ::term-change
 (fn [db [_ new-search-term]]
   (assoc db :search-term new-search-term)))

(defn- results->games-map
  [results]
  (reduce (fn [acc m]
            (assoc acc (:id m) m))
          {}
          results))

(rf/reg-event-fx
 ::successful-search
 (fn [{db :db} [_ response]]
   (let [body          (:body response)
         game-id->game (results->games-map (:results body))
         game-refs     (map #(vector :games %) (keys game-id->game))
         body          (assoc body :results game-refs)]
     {:fx [[::utils/log response]]
      :db (-> db
              (update :games merge game-id->game)
              (assoc :search-results body))})))

(rf/reg-event-fx
 ::failed-search
 (fn-traced [_ [_ response]]
   {:fx [[::utils/log response]]}))

(rf/reg-event-fx
 ::initiate-search
 [(rf/inject-cofx ::utils/server-hostname)]
 (fn-traced [{db :db
              hostname ::utils/server-hostname} _]
   ;; The implicit encoding of the locations for the api key and the search term feels wrong,
   ;; similar to the reasons we don't want to do that in view functions. My initial thought was to
   ;; create cofx handlers that would subscribe to the various values we need from the DB, since
   ;; those subscriptions already exist, but this doesn't mix well with the way that data flows
   ;; through a re-frame app (and is explicitly suggested against by
   ;; https://day8.github.io/re-frame/FAQs/UseASubscriptionInAnEventHandler/). After looking at one
   ;; library that would do
   ;; this (https://github.com/den1k/re-frame-utils/blob/master/src/vimsical/re_frame/cofx/inject.cljc)
   ;; and reviewing that docstring, it sounds like going down this route might create hidden
   ;; pitfalls for ourselves, and the more robust approach is to write functions that extract the
   ;; values from the DB and use those functions in event handlers, thus abstracting away from the
   ;; specifics.
   ;;
   ;; Aside from writing another function, it sounds like the idiomatic approach is to do what we're
   ;; doing below, or, at most, write a cofx handler that would do the same, except as an interceptor.
   ;;
   ;; My intuition tells me that the extra work involved in doing all of this isn't worth it yet, so
   ;; I'm going to leave this as-is, even though it has a "smell" to it.
   (let [{:keys [api-key
                 search-term]} db
         url                   (str hostname "/api/search")]
     {:fx [[:fetch {:method                 :get
                    :url                    url
                    :timeout                5000
                    :request-content-type   :json
                    :response-content-types {#"application/.*json" :json}
                    :params                 {:api_key api-key
                                             :format "json"
                                             :resources "game"
                                             :query  search-term}
                    :on-success             [::successful-search]
                    :on-failure             [::failed-search]}]]})))


;; -- Queries -------------------------------------------------------

(rf/reg-sub
 ::term
 (fn [db _]
   (:search-term db)))

(rf/reg-sub
 ::results
 (fn [db _]
   (:search-results db)))

(rf/reg-sub
 ::game
 (fn [db [_ game-ref]]
   (get-in db game-ref)))

(defn game-card
  [game-ref]
  (let [game @(rf/subscribe [::game game-ref])
        title (:name game)
        thumbnail-url (get-in game [:image :thumb_url])]
    [grid2 {:xs 2}
     [card
      [card-media {:component :img
                   :image thumbnail-url}]
      [card-content
       [typography {:gutterBottom true
                    :variant :h5
                    :component :div}
        title]]]]))

(defn search-results
  []
  (let [results (:results @(rf/subscribe [::results]))]
    [grid2 {:container true
            :spacing 4
            :flex-wrap :wrap}
     (for [game-ref results]
       ^{:key game-ref} [game-card game-ref])]))

(defn page
  []
  (let [emit        (fn [e] (rf/dispatch [::term-change (utils/event->value e)]))
        search-term @(rf/subscribe [::term])]
    [container {:maxWidth "lg"
                :sx {:margin-top 4
                     :display "flex"
                     :alignItems "center"
                     :flexDirection "column"
                     :row-gap "10px"}}
     [typography {:variant :h3}
      "What would you like to rent?"]
     [text-field {:label "Search Term"
                  :variant :outlined
                  :value search-term
                  :on-change emit
                  :on-key-down #(when (= (.-which %) 13) (rf/dispatch [::initiate-search]))}]
     [search-results]]))
