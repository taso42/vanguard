(ns user
  (:require
    [clojure.edn :as edn]
    [vanguard.web :as web])
  (:import
    org.openqa.selenium.chrome.ChromeDriver
    org.openqa.selenium.By))


(defn foo
  []
  (let [settings (edn/read-string (slurp "settings.edn"))
        driver   (web/connect (:start-page settings))]
    (web/log-on driver (:username settings) (:password settings))
    ;;(web/navigate-to-account driver (:account-link-text settings))
    (.get driver (:account-link settings))
    driver))


(comment
  (def driver (foo)))