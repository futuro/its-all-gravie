(ns software.justenough.itsallgravie.core
  "This namespace was originally copied from the re-frame \"simple\" example app, specifically this
  file:
  https://github.com/day8/re-frame/blob/b33956ef15f09bf6200fc0f97e9cb1db87e3a3cc/examples/simple/src/simple/core.cljs

  The reasoning behind this decision is laid out in the `dev-log.org` file, but I have since altered
  it almost completely. Still, I wanted to give credit where it's due, for getting me off the
  ground."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            [software.justenough.itsallgravie.api-key :as api-key]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [clojure.string :as str]))

;; Helpers

(defn event->value
  "Given an event, returns the event's target's value."
  [e]
  (-> e .-target .-value))

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db
 :initialize
 (fn [_ _]
   {:api-key        ""
    :cart           []
    :search-term    ""
    :search-results []
    :rented-items   []}))

(rf/reg-event-db
 :search-term-change
 (fn [db [_ new-search-term]]
   (assoc db :search-term new-search-term)))

(rf/reg-event-fx
 :successful-search
 (fn [{db :db} [_ response]]
   {:fx [[:log response]]
    :db (assoc db :search-results (:body response))}))

(rf/reg-event-fx
 :failed-search
 (fn-traced [_ [_ response]]
   {:fx [[:log response]]}))

(rf/reg-event-fx
 :search
 [(rf/inject-cofx ::server-hostname)]
 (fn-traced [{db :db
              hostname ::server-hostname} _]
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
                    :on-success             [:successful-search]
                    :on-failure             [:failed-search]}]]})))

;; -- Domino 3 - Effects -------------------------------------------------------

(rf/reg-fx
 :log
 (fn [value]
   (js/console.log value)))

(rf/reg-cofx
 ::server-hostname
 ;; This feels like a hacky method of getting our hostname in both dev and prod environments. It
 ;; feels hacky, and I'd rather have a config variable, but that would require injecting the
 ;; hostname during build time and I don't really want to do that at the moment, preferring instead
 ;; to get a proof of concept up and running.
 (fn insert-server-hostname
   [cofx]
   (assoc cofx ::server-hostname (.-location js/window))))

;; -- Domino 4 - Query  -------------------------------------------------------

(rf/reg-sub
 :search-term
 (fn [db _]
   (:search-term db)))

;; -- Domino 5 - View Functions ----------------------------------------------

(defn search-input
  []
  (let [emit        (fn [e] (rf/dispatch [:search-term-change (event->value e)]))
        search-term @(rf/subscribe [:search-term])]
    [:div
     "Search Term: "
     [:input {:type  "text"
              :style {:border "1px solid #CCC"}
              :value search-term
              :on-change emit
              :on-key-down #(when (= (.-which %) 13) (rf/dispatch [:search]))}]]))

(defn ui
  []
  [:div
   [:h1 "BlockBuster Forever"]
   [api-key/input]
   [search-input]])

;; -- Entry Point -------------------------------------------------------------

(defn mount-ui
  []
  (rdom/render [ui]
               (js/document.getElementById "app")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  ;; The `:dev/after-load` metadata causes this function to be called
  ;; after shadow-cljs hot-reloads code. We force a UI update by clearing
  ;; the Reframe subscription cache.
  (rf/clear-subscription-cache!)
  (mount-ui))

(defn run               ;; Your app calls this when it starts. See shadow-cljs.edn :init-fn.
  []
  (rf/dispatch-sync [:initialize]) ;; put a value into application state
  (mount-ui))                      ;; mount the application's ui into '<div id="app" />'
