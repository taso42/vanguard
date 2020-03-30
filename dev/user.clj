(ns user
  (:require
    [clojure.edn :as edn]))


(def settings (edn/read-string (slurp "settings.edn")))


(def raw-data
  [{:symbol "VGENX" :name "Vanguard Energy Fund Investor Shares (Cash) " :amount 39.67}
   {:symbol "VGENX" :name "Vanguard Energy Fund Investor Shares " :amount 4684.61}
   {:symbol "VIPSX" :name "Vanguard Inflation-Protected Securities Fund Investor Shares (Cash) " :amount 258.35}
   {:symbol "VIPSX" :name "Vanguard Inflation-Protected Securities Fund Investor Shares " :amount 44768.6}
   {:symbol "VGSLX" :name "Vanguard Real Estate Index Fund Admiral Shares " :amount 6235.51}
   {:symbol "VBTLX" :name "Vanguard Total Bond Market Index Fund Admiral Shares " :amount 44988.17}
   {:symbol "VTABX" :name "Vanguard Total International Bond Index Fund Admiral Shares " :amount 37622.61}
   {:symbol "VTIAX" :name "Vanguard Total International Stock Index Fund Admiral Shares (Cash) " :amount 1808.01}
   {:symbol "VTIAX" :name "Vanguard Total International Stock Index Fund Admiral Shares " :amount 60305.74}
   {:symbol "VTSAX" :name "Vanguard Total Stock Market Index Fund Admiral Shares (Cash) " :amount 979.45}
   {:symbol "VTSAX" :name "Vanguard Total Stock Market Index Fund Admiral Shares " :amount 72847.9}
   {:symbol "BRK B" :name "BERKSHIRE HATHAWAY INC CL B NEW " :amount 78152.1}])
