(ns vanguard.balance
  (:require
    [vanguard.util :as u]
    [clojure.pprint])
  (:gen-class))


(defn rebalance
  "rebalance by annotating each account record with a :target-amount and a :delta.
  The :delta indicates how much to buy or sell to bring into balance"
  [account total]
  (reduce-kv
    (fn [m ticker {:keys [amount target-allocation] :or {target-allocation 0} :as record}]
      (let [target%       (double (/ target-allocation 100))
            target-amount (u/round (* target% total) 2)
            delta         (u/round (- target-amount amount) 2)]
        (assoc m ticker (assoc record :target-amount target-amount :delta delta))))
    account
    account))


(defn sum-by
  "returns the sum all the values of `field` in the account"
  [account field]
  (apply + (map #(get (second %) field) account)))


(defn ->ticker-map
  "transform account vector into a map keyed by ticker symbol. result looks like
  `\"TICKER\" {:amount XXX}`"
  [account]
  (reduce
    (fn [m {:keys [symbol amount name]}]
      (assoc m symbol {:amount amount :name name}))
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


(defn print-summary
  [account]
  (printf "%-60s %-5s   %-10s %-10s %-10s\n" "NAME" "SYMBOL" "AMOUNT" "TARGET" "DELTA")
  (doseq [[symbol {:keys [name amount target-amount delta]}] account]
    (printf "%-60s (%-5s): %,10.2f %,10.2f %,10.2f\n" name symbol amount target-amount delta))
  (println (apply str (repeat 102 \-)))
  (let [current-balance (sum-by account :amount)
        target-balance  (sum-by account :target-amount)]
    (printf "%s %,10.2f %,10.2f\n" (apply str (repeat 69 " ")) current-balance target-balance)))


(defn -main
  [& args]
  (let [cash-to-add       (if (first args)
                            (read-string (first args))
                            0)
        target-allocation (:target-allocation (u/load-edn "settings.edn"))
        account           (merge-target-allocation (->ticker-map (u/load-edn "account.edn")) target-allocation)
        total             (+ cash-to-add (sum-by account :amount))
        rebalanced        (rebalance account total)]
    (print-summary rebalanced)))
