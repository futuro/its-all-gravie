(ns software.justenough.itsallgravie.cart
  (:require [re-frame.core :as rf]
            [software.justenough.itsallgravie.db :as db]))

(rf/reg-sub
 ::current-cart
 (fn [db _]
   (:cart db)))

(rf/reg-event-db
 ::add-to-cart
 [db/persist-db-intercepter]
 (fn [db [_ game-ref]]
   (update db :cart conj game-ref)))

(rf/reg-event-db
 ::remove-from-cart
 [db/persist-db-intercepter]
 (fn [db [_ game-ref]]
   (update db :cart disj game-ref)))
