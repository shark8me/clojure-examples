(defproject clojure-examples "NOT_DEPLOYED"
; :source-paths ["src/clj" ]
 :dependencies [[org.clojure/clojure "1.6.0"]
                 [rmarianski/tidy-up "0.0.2"]
                 [markdown-clj "0.9.63"]
                 [enlive "1.1.5"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [zip-visit "1.0.2"]
                 [gorilla-indented-html-renderer "0.1.0"]
                 [hiccup "1.0.5"]]
 :plugins [[lein-gorilla "0.3.4"]]
 :target-path "target/%s"
 :profiles {:uberjar {:aot :all}}
 )
