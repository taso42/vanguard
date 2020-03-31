(ns vanguard.web
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


;; maybe not needed if `:account-link` is a durable link
#_(defn navigate-to-account
    [^RemoteWebDriver driver account-link-text]
    (doto
      (WebDriverWait. driver 10)
      (.until (ExpectedConditions/elementToBeClickable (By/linkText account-link-text))))
    (let [^RemoteWebElement account-link (.findElement driver (By/linkText account-link-text))]
      (.click account-link)))

(defn- $->number
  [amount-str]
  (-> (clojure.string/replace amount-str #"\$" "")
      (clojure.string/replace #"," "")
      read-string))

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
       :amount ($->number (nth row 7))}

      ;; stock
      (= 12 (count row))
      {:symbol (nth row 0)
       :name   (nth row 1)
       :amount ($->number (nth row 7))})))


(defn parse-account-table
  [^RemoteWebElement table]
  (reduce
    (fn [rows ^RemoteWebElement row]
      (if-let [row (parse-account-row row)]
        (conj rows row)
        rows))
    []
    (.findElements table (By/tagName "tr"))))
