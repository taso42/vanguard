(defproject vanguard "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.seleniumhq.selenium/selenium-java "3.141.59"]]

  :main ^:skip-aot vanguard.core

  :target-path "target/%s"

  :global-vars {*warn-on-reflection* true}

  :aliases {"fetch"     ["with-profile" "fetch" "run"]
            "rebalance" ["with-profile" "rebalance" "run"]}

  :profiles {:uberjar   {:aot :all}

             :repl      {:dependencies [[vvvvalvalval/scope-capture "0.3.2"]]
                         :injections   [(require 'sc.api)]}

             :fetch     {:main vanguard.core}

             :rebalance {:main vanguard.balance}})
