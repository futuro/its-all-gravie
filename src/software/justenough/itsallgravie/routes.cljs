(ns software.justenough.itsallgravie.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [re-frame.core :as rf]
            [software.justenough.itsallgravie.db :as db]))

(rf/reg-sub
 ::current-page
 (fn [db _]
   (:current-page db)))

(rf/reg-event-db
 ::change-page
 [db/persist-db-intercepter]
 (fn-traced [db [_ page-key]]
   (assoc db :current-page page-key)))

(def routes
  ["/" {#{"" "home"} :home
        "search"     :search
        "checkout"   :checkout}])

(defn dispatch-page
  [event]
  (rf/dispatch [::change-page event]))

(defn match-fn
  "This takes a string from pushy representing the URL's path and search params (if relevant) that
  we're trying to navigate to. "
  [route-str]
  (bidi/match-route routes route-str))

(defonce history
  (pushy/pushy dispatch-page match-fn :identity-fn :handler))

;; Pushy automatically calls `pushy/stop!` on our history var when we call `pushy/start!`, so I'm
;; not going to handle that myself.
(pushy/start! history)
