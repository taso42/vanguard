# Vanguard Portolio Rebalancer 

Client for [Vanguard](https://investor.vanguard.com/home) that grabs account balances, by web scraping with Selenium, 
and performs rebalance calculations.

Output is text (doesn't place orders, yet).

## usage

Copy `settings.eg.edn` to `settings.edn` and populate.

```clojure
lein fetch
```
Fetch account balances. Output goes to `account.edn`. 
This will span a browser. You may be required to enter an MFA code.


```clojure
lein rebalance
```
Rebalance account (based on `account.edn`). Prints a summary of the current holdings, target, and delta.
