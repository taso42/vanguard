(ns vanguard.core
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as str]
    [clojure.tools.cli :refer [parse-opts]]
    [vanguard.web :as web]
    [vanguard.util :as u])
  (:import
    (org.openqa.selenium By)
    (org.openqa.selenium.support.ui WebDriverWait ExpectedConditions)
    (org.openqa.selenium.remote RemoteWebDriver))
  (:gen-class))


(defn login
  [username password start-page account-page]
  (let [^RemoteWebDriver driver (web/connect start-page)]
    (web/log-on driver username password)
    (println "waiting for login")
    (doto
      (WebDriverWait. driver 120)
      (.until (ExpectedConditions/presenceOfElementLocated (By/id "BalancesTabBoxId_tabBoxItemLink0"))))
    (println "logged in.")
    (.get driver account-page)
    driver))


(defn scrape-account-data
  [^RemoteWebDriver driver account-table-id]
  (doto
    (WebDriverWait. driver 10)
    (.until (ExpectedConditions/presenceOfElementLocated (By/id account-table-id))))
  (let [table (.findElement driver (By/id account-table-id))]
    (web/parse-account-table table)))


(defn- trim-name
  [name]
  (-> (str/replace name #"\(Cash\) $" "")
      (str/trim)))


(defn squash-cash-holdings
  [rows]
  (let [squashed
        (reduce
          (fn [m {:keys [symbol name amount] :as record}]
            (if (get m symbol)
              (update-in m [symbol :amount] (fn [a] (+ a amount)))
              (assoc m symbol (assoc record :name (trim-name name)))))
          {}
          rows)]
    (mapv #(get squashed %) (keys squashed))))


(def cli-options
  ;; An option with a required argument
  [["-s" "--settings file" "settings file"
    :default "settings.edn"]

   ;; A boolean option defaulting to nil
   ["-h" "--help"]])


(defn -main
  [& args]
  (let [opts                    (parse-opts args cli-options)
        settings                (u/load-edn (get-in opts [:options :settings]))
        ^RemoteWebDriver driver (login (:username settings)
                                       (:password settings)
                                       (:start-page settings)
                                       (:account-page settings))]
    (try
      (->> (reduce-kv
             (fn [account-data name {:keys [account-table-id]}]
               (let [account (-> (scrape-account-data driver account-table-id)
                                 (squash-cash-holdings))]
                 (assoc account-data name account)))
             {}
             (:accounts settings))
           (spit (or (:output-file settings) "accounts.edn")))
      (finally
        (.quit driver)))))
