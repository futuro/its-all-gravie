(ns software.justenough.itsallgravie.routes
  (:require [bidi.bidi :as bidi]
            [pushy.core :as pushy]
            [day8.re-frame.tracing :refer-macros [fn-traced]]
            [re-frame.core :as rf]))

(rf/reg-sub
 ::current-page
 (fn [db _]
   (:current-page db)))

(rf/reg-event-db
 ::change-page
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

(defn ^:dev/before-load stop-pushy-listeners
  "The before-load metadata tells shadow cljs to run this function before loading the new code, and
  this way we can ensure that the event listeners registered by `pushy/start!` for the old `history`
  var are cleaned up, as they'd otherwise be left running."
  []
  (pushy/stop! history))

(pushy/start! history)
