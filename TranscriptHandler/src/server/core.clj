(ns server.core
  (:require [server.sentences   :refer [strings->sentences]]
            [server.charset     :refer [wrap-charset]]
            [compojure.core     :refer :all]
            [compojure.handler  :refer [api]]
            [ring.util.response :refer [charset response]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clj-http.client    :as client]))

; this serves as a data queue
(def snippets (repeatedly promise))

(def translator "http://localhost:3001/translate")

(defn translate [text]
  (future
    (:body (client/post translator {:body text}))))

(def translations
  (delay
    (map translate (strings->sentences (map deref snippets)))))

(defn accept-snippet [n text]
  (deliver (nth snippets n) text))

(defn get-translation [n] 
  @(nth @translations n))

(defroutes app-routes
  (PUT "/snippet/:n" [n :as {:keys [body]}]
    (accept-snippet (Integer. n) (slurp body))
    (response "OK"))
  (GET "/translation/:n" [n] 
    (response (get-translation (Integer. n)))))

(defn -main [& args]
  (run-jetty (wrap-charset (api app-routes)) {:port 3000}))
