(ns vanguard.core
  (:require
    [clojure.edn :as edn]
    [clojure.pprint :refer [pprint]]
    [vanguard.web :as web])
  (:import
    (org.openqa.selenium By)
    (org.openqa.selenium.support.ui WebDriverWait ExpectedConditions)
    (org.openqa.selenium.remote RemoteWebDriver))
  (:gen-class))


(defn- cleanup-account-data
  [raw-data]
  (for [record raw-data]
    (update record :amount
            (fn [amount-str]
              (-> (clojure.string/replace amount-str #"\$" "")
                  (clojure.string/replace #"," "")
                  read-string)))))


(defn login
  []
  (let [settings                (edn/read-string (slurp "settings.edn"))
        ^RemoteWebDriver driver (web/connect (:start-page settings))]
    (web/log-on driver (:username settings) (:password settings))
    (println "waiting for login")
    (doto
      (WebDriverWait. driver 120)
      (.until (ExpectedConditions/presenceOfElementLocated (By/id "BalancesTabBoxId_tabBoxItemLink0"))))
    (println "logged in.")
    driver))


(defn scrape-account-data
  []
  (let [settings                (edn/read-string (slurp "settings.edn"))
        ^RemoteWebDriver driver (login)]
    (try
      (.get driver (:account-link settings))
      (doto
        (WebDriverWait. driver 10)
        (.until (ExpectedConditions/presenceOfElementLocated (By/id (:account-table-id settings)))))
      (let [table (.findElement driver (By/id (:account-table-id settings)))]
        (cleanup-account-data (web/parse-account-table table)))
      (finally
        (.quit driver)))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (pprint (scrape-account-data)))