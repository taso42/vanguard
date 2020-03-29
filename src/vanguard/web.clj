(ns vanguard.web
  (:import
    (org.openqa.selenium.chrome ChromeDriver)
    (org.openqa.selenium By)
    (org.openqa.selenium.support.ui WebDriverWait ExpectedConditions)))


(System/setProperty "webdriver.chrome.driver" "/Users/taso/Downloads/chromedriver")


(defn populate
  [element content]
  (.clear element)
  (.sendKeys element (into-array java.lang.CharSequence [content])))


(defn log-on
  [driver username password]
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
    ;; block until log on is possible
    (doto
      (WebDriverWait. driver 10)
      (.until (ExpectedConditions/elementToBeClickable (By/id "LoginForm:submitInput"))))
    driver))


;; maybe not needed if the direct link is permanent
(defn navigate-to-account
  [driver account-link-text]
  (doto
    (WebDriverWait. driver 10)
    (.until (ExpectedConditions/elementToBeClickable (By/linkText account-link-text))))
  (let [account-link (.findElement driver (By/linkText account-link-text))]
    (.click account-link)))