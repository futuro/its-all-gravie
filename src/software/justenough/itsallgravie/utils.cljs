(ns software.justenough.itsallgravie.utils
  (:require [re-frame.core :as rf]))

(defn event->value
  "Given an event, returns the event's target's value."
  [e]
  (-> e .-target .-value))

(rf/reg-fx
 ::log
 (fn [value]
   (when goog.DEBUG
     (js/console.log value))))

(rf/reg-cofx
 ::server-hostname
 ;; This feels like a hacky method of getting our hostname in both dev and prod environments. It
 ;; feels hacky, and I'd rather have a config variable, but that would require injecting the
 ;; hostname during build time and I don't really want to do that at the moment, preferring instead
 ;; to get a proof of concept up and running.
 (fn insert-server-hostname
   [cofx]
   (assoc cofx ::server-hostname (.. js/window -location -origin))))
