(ns vanguard.util
  (:require
    [clojure.edn :as edn]
    [clojure.string :as str]))


(defn round
  [n s]
  (double (.setScale ^java.math.BigDecimal (bigdec n) (int s) java.math.RoundingMode/HALF_UP)))


(defn load-edn
  [file]
  (edn/read-string (slurp file)))


(defn $->number
  [amount-str]
  (-> (str/replace amount-str #"\$" "")
      (str/replace #"," "")
      read-string
      (round 2)))