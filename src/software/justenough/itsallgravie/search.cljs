(ns software.justenough.itsallgravie.search
  (:require [clojure.string :as str]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [software.justenough.itsallgravie.utils :as utils]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]))

;; -- Events -------------------------------------------------------

(rf/reg-event-db
 ::term-change
 (fn [db [_ new-search-term]]
   (assoc db :search-term new-search-term)))

(rf/reg-event-fx
 ::successful-search
 (fn [{db :db} [_ response]]
   {:fx [[::utils/log response]]
    :db (assoc db :search-results (:body response))}))

(rf/reg-event-fx
 ::failed-search
 (fn-traced [_ [_ response]]
   {:fx [[::utils/log response]]}))

(rf/reg-event-fx
 ::initiate-search
 [(rf/inject-cofx ::utils/server-hostname)]
 (fn-traced [{db :db
              hostname ::utils/server-hostname} _]
   ;; TODO: The implicit encoding of knowledge of where the search-term and api-key live in the DB
   ;; feels like the wrong approach, at least when viewed in a similar like as to why we don't pull
   ;; things right from the DB in a view, but instead subscribe to a previously registered Query.
   ;; That said, if we subscribe to those Queries, they'll get rerun when the key or term are
   ;; updated, which would then trigger this to rerun, I believe, and we don't want that.
   ;;
   ;; Hmm...gonna have to ponder this some more later.
   (let [{:keys [api-key
                 search-term]} db
         url                   (str hostname "api/search")]
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

(defn input
  []
  (let [emit        (fn [e] (rf/dispatch [::term-change (utils/event->value e)]))
        search-term @(rf/subscribe [::term])]
    [:div
     "Search Term: "
     [:input {:type  "text"
              :style {:border "1px solid #CCC"}
              :value search-term
              :on-change emit
              :on-key-down #(when (= (.-which %) 13) (rf/dispatch [::initiate-search]))}]]))

