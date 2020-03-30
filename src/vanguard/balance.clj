(ns vanguard.balance
  (:require
    [clojure.edn :as edn]
    [vanguard.core :as v])
  (:gen-class))



(defn -main
  [& args]
  (let [account (v/load-edn "account.edn")]
    (clojure.pprint/pprint account)))