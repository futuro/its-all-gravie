(ns software.justenough.itsallgravie.db
  (:require [akiroz.re-frame.storage :as local-storage]
            [re-frame.core :as rf]))

(def LOCAL-STORAGE-KEY
  :its-all-gravie)

(def db-keys-to-persist
  [:api-key
   :current-page
   :cart
   :games
   :rented-games])

(def persist-db-intercepter
  "An intercepter that merges in the local storage values as a `:before` action, and then persists the
  given keys back into local storage `:after`."
  (local-storage/persist-db-keys LOCAL-STORAGE-KEY db-keys-to-persist))

(rf/reg-event-db
 ::initialize
 [persist-db-intercepter]
 (fn [db _]
   (merge {:api-key        ""
           :current-page   "home"
           :cart           #{}
           :games          {}
           :search-term    ""
           :search-results []
           :rented-games   []}
          db)))
