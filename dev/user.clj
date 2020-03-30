(ns user
  (:require
    [clojure.edn :as edn]
    [vanguard.web :as web])
  (:import
    (org.openqa.selenium By)
    (org.openqa.selenium.remote RemoteWebDriver RemoteWebElement)
    (org.openqa.selenium.support.ui ExpectedConditions WebDriverWait)))


(def settings (edn/read-string (slurp "settings.edn")))


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


(defn scrape-account
  []
  (let [settings                (edn/read-string (slurp "settings.edn"))
        ^RemoteWebDriver driver (web/connect (:start-page settings))]
    (try
      (web/log-on driver (:username settings) (:password settings))
      (.get driver (:account-link settings))
      (doto
        (WebDriverWait. driver 10)
        (.until (ExpectedConditions/presenceOfElementLocated (By/id (:account-table-id settings)))))
      (let [table (.findElement driver (By/id (:account-table-id settings)))]
        (web/parse-account-table table))
      (finally
        (.quit driver)))))


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


(doto
  (WebDriverWait. driver 5)
  (.until (ExpectedConditions/presenceOfElementLocated (By/partialLinkText "Welcome back"))))