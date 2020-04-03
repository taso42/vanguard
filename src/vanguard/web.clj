(ns vanguard.web
  (:require
    [vanguard.util :as u])
  (:import
    (org.openqa.selenium.chrome ChromeDriver)
    (org.openqa.selenium By)
    (org.openqa.selenium.support.ui WebDriverWait ExpectedConditions)
    (org.openqa.selenium.remote RemoteWebElement RemoteWebDriver)))


(System/setProperty "webdriver.chrome.driver" "/Users/taso/Downloads/chromedriver")


(defn populate
  [^RemoteWebElement element content]
  (.clear element)
  (.sendKeys element (into-array java.lang.CharSequence [content])))


(defn log-on
  [^RemoteWebDriver driver username password]
  (let [user-field (.findElement driver (By/id "LoginForm:USER"))
        pass-field (.findElement driver (By/id "LoginForm:PASSWORD-blocked"))
        submit     (.findElement driver (By/id "LoginForm:submitInput"))]
    (populate user-field username)
    (populate pass-field password)
    (.click submit)))


(defn connect
  [start-page]
  (let [driver (doto (ChromeDriver.)
                 (.get start-page))]
    ;; block until login is possible
    (doto
      (WebDriverWait. driver 10)
      (.until (ExpectedConditions/elementToBeClickable (By/id "LoginForm:submitInput"))))
    driver))


(defn parse-account-row
  [^RemoteWebElement row]
  (let [row
        (reduce
          (fn [elements ^RemoteWebElement element]
            (conj elements (.getText element)))
          []
          (.findElements row (By/tagName "td")))]
    (cond
      ;; mutual fund
      (= 14 (count row))
      {:symbol (nth row 0)
       :name   (nth row 1)
       :amount (u/$->number (nth row 7))}

      ;; mutual fund alternate
      (= 15 (count row))
      {:symbol (nth row 0)
       :name   (nth row 1)
       :amount (u/$->number (nth row 8))}

      ;; stock
      (= 12 (count row))
      {:symbol (nth row 0)
       :name   (nth row 1)
       :amount (u/$->number (nth row 7))})))


(defn parse-account-table
  [^RemoteWebElement table]
  (reduce
    (fn [rows ^RemoteWebElement row]
      (if-let [row (parse-account-row row)]
        (conj rows row)
        rows))
    []
    (.findElements table (By/tagName "tr"))))
