(ns software.justenough.itsallgravie.utils)

(defn event->value
  "Given an event, returns the event's target's value."
  [e]
  (-> e .-target .-value))
