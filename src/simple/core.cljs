(ns simple.core
  "This namespace was originally copied from the re-frame \"simple\" example app, specifically this
  file:
  https://github.com/day8/re-frame/blob/b33956ef15f09bf6200fc0f97e9cb1db87e3a3cc/examples/simple/src/simple/core.cljs

  The reasoning behind this decision is laid out in the `dev-log.org` file, but I have since altered
  it almost completely. Still, I wanted to give credit where it's due, for getting me off the
  ground."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [day8.re-frame.tracing :refer-macros [fn-traced]]))

;; Helpers

(defn event->value
  "Given an event, returns the event's target's value."
  [e]
  (-> e .-target .-value))

;; -- Domino 2 - Event Handlers -----------------------------------------------

(rf/reg-event-db              ;; sets up initial application state
 :initialize                 ;; usage:  (dispatch [:initialize])
 (fn [_ _]                   ;; the two parameters are not important here, so use _
   {:search-term    ""
    :search-results []
    :cart           []
    :rented-items   []}))

(rf/reg-event-db
 :search-term-change
 (fn [db [_ new-search-term]]
   (assoc db :search-term new-search-term)))

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
              :value @(rf/subscribe [:search-term])
              :on-change emit}]]))

(defn ui
  []
  [:div
   [:h1 "BlockBuster Forever"]
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
