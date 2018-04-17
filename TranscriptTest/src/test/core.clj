(ns test.core
  (:require [test.jabberwocky :refer :all]
            [clj-http.client  :as client]))

; a thread to fetch the translation results
(future
  (doseq [n (iterate inc 0)]
    (println (:body (client/get (str "http://localhost:3000/translation/" n))))))

; main thread to put snippets to handler
(defn -main [& args]
  (doseq [n (range (count jabberwocky))]
    (client/put (str "http://localhost:3000/snippet/" n) {:body (nth jabberwocky n)})))