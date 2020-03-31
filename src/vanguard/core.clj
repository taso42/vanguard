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
  (let [opts          (parse-opts args cli-options)
        settings      (u/load-edn (get-in opts [:options :settings]))
        outfile       (or (:output-file settings) "account.edn")
        data          (-> (scrape-account-data settings)
                          squash-cash-holdings)]
    (println "output to" outfile)
    (spit outfile data)))
