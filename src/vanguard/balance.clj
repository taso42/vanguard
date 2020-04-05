(ns vanguard.balance
  (:require
    [vanguard.util :as u]
    [clojure.tools.cli :refer [parse-opts]]
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
            delta         (u/round (- target-amount amount) 2)
            actual%       (u/round (* 100 (/ amount total)) 1)
            delta%        (- actual% target-allocation)]
        (assoc m ticker (assoc record :target-amount target-amount :delta delta :actual% actual% :delta% delta%))))
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


(defn allocate-composites
  [account composites]
  (reduce-kv
    (fn [account symbol components]
      (let [amount-to-split (get-in account [symbol :amount])
            account         (dissoc account symbol)]
        (reduce-kv
          (fn [account symbol pct]
            (update-in account [symbol :amount] #(u/round (+ % (* (float (/ pct 100)) amount-to-split)) 2)))
          account
          components)))
    account
    composites))


(defn print-summary
  [account]
  (printf "%-61s %-5s %12s %10s %4s %4s %5s %11s\n" "NAME" "SYMBOL" "AMOUNT" "TARGET" "TGT%" "%" "DEL%" "DELTA")
  (doseq [[symbol {:keys [name amount target-amount target-allocation actual% delta delta%]
                   :or   {target-allocation 0.0 actual% 0.0 delta% 0}}] account]
    (printf "%-61s (%-5s): %,10.2f %,10.2f %,4.1f %,4.1f %,5.1f %,11.2f\n"
            name symbol amount target-amount (float target-allocation) (float actual%) (float delta%) delta))
  (println (apply str (repeat 120 \-)))
  (let [current-balance (sum-by account :amount)
        target-balance  (sum-by account :target-amount)]
    (printf "%s %,10.2f %,10.2f\n" (apply str (repeat 69 " ")) current-balance target-balance)))


(def cli-options
  ;; An option with a required argument
  [["-s" "--settings FILE" "settings file"
    :default "settings.edn"]

   ["-a" "--account ACCOUNT" "account nickname"]

   ["-c" "--cash AMOUNT" "cash to add"
    :parse-fn #(Integer/parseInt %)
    :default 0]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])


(defn -main
  [& args]
  (let [opts              (parse-opts args cli-options)
        settings          (u/load-edn (get-in opts [:options :settings]))
        cash-to-add       (get-in opts [:options :cash])
        account-name      (get-in opts [:options :account])
        target-allocation (get-in settings [account-name :target-allocation])
        account           (-> (u/load-edn "accounts.edn")
                              (get account-name)
                              (->ticker-map)
                              (merge-target-allocation target-allocation)
                              (allocate-composites (:composite-funds settings)))
        total             (+ cash-to-add (sum-by account :amount))
        rebalanced        (rebalance account total)]
    (print-summary rebalanced)))
