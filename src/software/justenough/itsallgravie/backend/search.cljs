(ns software.justenough.itsallgravie.backend.search
  (:require [clojure.string :as str]
            [shadow.cljs.modern :refer (js-await)]))

(def gb-host
  "www.giantbomb.com")

(def fetch-options
  {:headers {"content-type" "application/json;charset=UTF-8"
             ;; GiantBomb requires that you set a unique User Agent, or it rejects your request.
             "User-Agent" "It's All Gravie/1.0"}})

(defn gather-response
  [{:keys [^js/Headers headers]
    :as ^js/Response response}]
  (let [content-type (or (.-content-type headers)
                         "")]
    (if (str/includes? content-type "application/json")
      (-> response
          (.json)
          (.then #(.stringify js/JSON %)))
      (.text response))))

(defn ^:export onRequest
  [{:keys [request params functionPath env]
    :as   context}]
  (let [req-url (.-url request)
        url     (js/URL. req-url)
        options (clj->js fetch-options)]
    (set! (.-hostname url) gb-host)
    (set! (.-protocol url) "https")
    (set! (.-port url) "")
    (js/console.log (str "Fetching results from: " url))
    (js/console.log (str "Using options: " options))
    (js-await [response (js/fetch url options)]
      (js-await [results (gather-response response)]
        (js/Response. results options)))))
