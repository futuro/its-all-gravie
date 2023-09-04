(ns software.justenough.itsallgravie.core
  "This namespace was originally copied from the re-frame \"simple\" example app, specifically this
  file:
  https://github.com/day8/re-frame/blob/b33956ef15f09bf6200fc0f97e9cb1db87e3a3cc/examples/simple/src/simple/core.cljs

  The reasoning behind this decision is laid out in the `dev-log.org` file, but I have since altered
  it almost completely. Still, I wanted to give credit where it's due, for getting me off the
  ground."
  (:require [clojure.string :as str]
            [reagent.dom :as rdom]
            [re-frame.core :as rf]
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
            [software.justenough.itsallgravie.checkout :as checkout]
            [software.justenough.itsallgravie.db :as db]))

;; Helpers

(defn event->value
  "Given an event, returns the event's target's value."
  [e]
  (-> e .-target .-value))

(defn url-tab
  [page-name]
  [tab {:label (str/capitalize page-name)
        :value (str/lower-case page-name)
        :href  (str "/" (str/lower-case page-name))}])

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
        (url-tab "home")
        (url-tab "search")
        (url-tab "checkout")]]]
     (case current-page
       :home [home/page]
       :search [search/page]
       :checkout [checkout/page]
       [home/page])]))

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

(defn run
  []
  (rf/dispatch-sync [::db/initialize])
  (mount-ui))
