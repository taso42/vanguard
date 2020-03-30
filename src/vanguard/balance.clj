(ns vanguard.balance
  (:require
    [vanguard.core :as v])
  (:gen-class))


(defn rebalance
  "rebalance by annotating each account record with a :target-amount and a :delta.
  The :delta indicates how much to buy or sell to bring into balance"
  [account total]
  (reduce-kv
    (fn [m ticker {:keys [amount target-allocation] :or {target-allocation 0} :as record}]
      (let [target%       (float (/ target-allocation 100))
            target-amount (* target% total)
            delta         (- target-amount amount)]
        (assoc m ticker (assoc record :target-amount target-amount :delta delta))))
    account
    account))


(defn sum-by
  "returns the sum all the values of `field` in the account"
  [account field]
  (reduce-kv
    (fn [tally _ record]
      (+ tally (get record field)))
    0
    account))


(defn ->ticker-map
  "transform account vector into a map keyed by ticker symbol. result looks like
  `\"TICKER\" {:amount XXX}`"
  [account]
  (reduce
    (fn [m {:keys [symbol amount]}]
      (assoc m symbol {:amount amount}))
    {}
    account))


(defn merge-target-allocation
  "annotate each account entry, adding a :target-allocation"
  [account target-allocation]
  (reduce-kv
    (fn [m ticker allocation]
      (assoc-in m [ticker :target-allocation] allocation))
    account
    target-allocation))


(defn -main
  [& args]
  (let [target-allocation (:target-allocation (v/load-edn "settings.edn"))
        account           (merge-target-allocation (->ticker-map (v/load-edn "account.edn")) target-allocation)
        total             (sum-by account :amount)]
    (clojure.pprint/pprint (rebalance account total))))
