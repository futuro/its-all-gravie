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

(defn persist-db-keys
  "This is an almost exact replica of `local-storage/persist-db-keys`, except that I've changed the
  `:before` function to merge the existing DB on top of the local storage data, instead of what it
  was doing before, which was merging the local store data on top of the live data.

  This was causing the collection of games to get overwritten with an empty set, which then
  invalidated our data.

  Longer term, I'd either submit a bug fix to the local storage library, or write my own
  interceptor, given how little code is required."
  [store-key db-keys]
  (local-storage/register-store store-key)
  (rf/->interceptor
    :id (keyword (str (apply str (sort db-keys)) "->" store-key))
    :before (fn [context]
              (update-in context [:coeffects :db] #(merge (local-storage/<-store store-key) %)))
    :after (fn [context]
             (when-let [value (some-> (get-in context [:effects :db])
                                      (select-keys db-keys))]
               (local-storage/->store store-key value))
             context)))

(def persist-db-intercepter
  "An intercepter that merges in the local storage values as a `:before` action, and then persists the
  given keys back into local storage `:after`."
  (persist-db-keys LOCAL-STORAGE-KEY db-keys-to-persist))

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
           :rented-games   #{}}
          db)))
