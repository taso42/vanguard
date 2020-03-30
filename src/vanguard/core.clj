(ns vanguard.core
  (:require
    [vanguard.web :as web])
  (:gen-class))


(defn cleanup-account-record
  [record]
  (update record :amount
          (fn [amount-str]
            (-> (clojure.string/replace amount-str #"\$" "")
                (clojure.string/replace #"," "")
                read-string))))


(defn cleanup-account-data
  [raw-data]
  (for [record raw-data]
    (cleanup-account-record record)))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]

  (println "Hello, World!"))