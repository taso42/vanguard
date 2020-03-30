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


(defn login
  [settings]
  (let [^RemoteWebDriver driver (web/connect (:start-page settings))]
    (web/log-on driver (:username settings) (:password settings))
    (println "waiting for login")
    (doto
      (WebDriverWait. driver 120)
      (.until (ExpectedConditions/presenceOfElementLocated (By/id "BalancesTabBoxId_tabBoxItemLink0"))))
    (println "logged in.")
    driver))


(defn scrape-account-data
  [settings]
  (let [^RemoteWebDriver driver (login settings)]
    (try
      (.get driver (:account-link settings))
      (doto
        (WebDriverWait. driver 10)
        (.until (ExpectedConditions/presenceOfElementLocated (By/id (:account-table-id settings)))))
      (let [table (.findElement driver (By/id (:account-table-id settings)))]
        (web/parse-account-table table))
      (finally
        (.quit driver)))))


(defn -main
  [& args]
  (let [outfile (or (first args) "account.edn")
        data    (scrape-account-data (edn/read-string (slurp "settings.edn")))]
    (println "output to" outfile)
    (spit outfile data)))