(ns simple.proxy
  "This namespace is copied almost verbatim from
  https://shadow-cljs.github.io/docs/UsersGuide.html#NodeHotCodeReload, to save me the time required
  to sort out building a node proxy server."
  (:require [cljs.pprint :as pp]
            ["http" :as http]
            ["https" :as https]))

(def giantbomb-prefix "https://www.giantbomb.com")

(defn res-on-data
  "Puts all of the data from the response object into the given atom."
  [body-atom ])

(defn res-on-end
  "Turns the response body into json"
  [body-atom])

(defn handle-proxying
  [original-response]
  (fn gb-request
    [gb-response]
    (let [body-atom (atom "")
          status (.-statusCode gb-response)]
      ;; Since I want this to be a transparent proxy -- or at least as much of one as I can hack out
      ;; in a day or less -- I'm not going to do anything with errors here, but instead try to pass
      ;; that back to the FE and let the FE handle it.
      ())
    ;; TODO: I believe I need to register event handlers a la `res.on('data')`, see
    ;; https://stackoverflow.com/questions/19539391/how-to-get-data-out-of-a-node-js-http-get-request
    ;; for an example of the JS version.
    (js/console.log gb-response)
    ;; I have no idea if this works, lol
    (.end original-response "foo")))

;; TODO: This needs to forward the request to GiantBomb, and then return the response to the caller.
(defn request-handler [req res]
  (let [url (.-url req)]
    ;; N.B. this requires that the url passed by the frontend is well formed, and thus is super
    ;; brittle.
    (https/get (str giantbomb-prefix url) (handle-proxying res))))

; a place to hang onto the server so we can stop/start it
(defonce server-ref
  (volatile! nil))

(defn main [& args]
  (js/console.log "starting server")
  (let [server (http/createServer #(request-handler %1 %2))]

    (.listen server 3000
      (fn [err]
        (if err
          (js/console.error "server start failed")
          (js/console.info "http server running"))
        ))

    (vreset! server-ref server)))

(defn start
  "Hook to start. Also used as a hook for hot code reload."
  []
  (js/console.warn "start called")
  (main))

(defn stop
  "Hot code reload hook to shut down resources so hot code reload can work"
  [done]
  (js/console.warn "stop called")
  (when-some [srv @server-ref]
    (.close srv
      (fn [err]
        (js/console.log "stop completed" err)
        (done)))))

(js/console.log "__filename" js/__filename)
