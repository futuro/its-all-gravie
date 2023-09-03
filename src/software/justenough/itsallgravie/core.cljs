(ns software.justenough.itsallgravie.core
  "This namespace was originally copied from the re-frame \"simple\" example app, specifically this
  file:
  https://github.com/day8/re-frame/blob/b33956ef15f09bf6200fc0f97e9cb1db87e3a3cc/examples/simple/src/simple/core.cljs

  The reasoning behind this decision is laid out in the `dev-log.org` file, but I have since altered
  it almost completely. Still, I wanted to give credit where it's due, for getting me off the
  ground."
  (:require [clojure.string :as str]
            [reagent.dom.client :as rdomc]
            [re-frame.core :as rf]
            ["react" :as react]
            ;; Needed for the `:fetch` effect handler
            [superstructor.re-frame.fetch-fx]
            ;; Our code
            [software.justenough.itsallgravie.api-key :as api-key]
            [software.justenough.itsallgravie.search :as search]
            [software.justenough.itsallgravie.routes :as routes]
            ;; MUI components
            [reagent-mui.material.app-bar :refer [app-bar]]
            [reagent-mui.material.box :refer [box]]
            [reagent-mui.material.link :refer [link]]
            [reagent-mui.material.typography :refer [typography]]
            [reagent-mui.material.toolbar :refer [toolbar]]
            [reagent-mui.material.tabs :refer [tabs]]
            [reagent-mui.material.tab :refer [tab]]
            ;; Development aids
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [software.justenough.itsallgravie.home :as home]
            [software.justenough.itsallgravie.checkout :as checkout]))

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
    :current-page   "home"
    :cart           #{}
    :games          {}
    :search-term    ""
    :search-results []
    :rented-items   []}))

;; -- Domino 5 - View Functions ----------------------------------------------

(defn ui
  []
  (let [current-page @(rf/subscribe [::routes/current-page])
        change-page #(routes/dispatch-page %)]
    [box {:style {:flex-wrap :wrap
                  :flex-direction :column}
          :sx {:flexGrow 1
               :display "flex"}}
     [app-bar
      {:position "static"}
      [toolbar
       [typography
        {:variant :h2
         :component :div
         :sx {:flexGrow 1}}
        [link
         {:href "/"
          :color "primary.contrastText"
          :underline "none"}
         "It's All Gravie: the game library!"]]
       [tabs
        {:value current-page
         :sx {:color "primary.contrastText"}
         :textColor "inherit"
         :indicatorColor "secondary"}
        [tab {:component :a
              :value "home"
              :label "Home"
              :href "/home"}]
        [tab {:component :a
              :label "Search"
              :value "search"
              :href "/search"}]
        [tab {:component :a
              :label "Checkout"
              :value "checkout"
              :href "/checkout"}]]]]
     (case current-page
       :home [home/page]
       :search [search/page]
       :checkout [checkout/page]
       [home/page])]))

;; -- Entry Point -------------------------------------------------------------

(defonce react-root
  (rdomc/create-root (js/document.getElementById "app")))

(defn mount-ui
  []
  (rdomc/render react-root [ui]))

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
