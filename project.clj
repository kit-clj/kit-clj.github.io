(defproject kit-docs "0.1"
  :description "Documentation site for the Kit framework"
  :url "https://kit-clj.github.io/"
  :license {:name "MIT License"
            :url  "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [hiccup "1.0.5"]
                 [markdown-clj "1.11.2"]
                 [crouton "0.1.2"]
                 [luminus/config "0.8"]
                 [selmer "1.12.53"]
                 [me.raynes/fs "1.4.6"]]
  :min-lein-version "2.0.0"
  :main kit.core)
