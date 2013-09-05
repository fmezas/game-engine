(defproject tanks "1.0.0-SNAPSHOT"
  :description "basic game engine in clojure with sample game"
  :url "https://github.com/fmezas/game-engine#readme"
  :main tanks.core
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/math.numeric-tower "0.0.2"]]
  :profiles {:dev {:dependencies [[midje "1.5-alpha8"]],
                   :plugins [[lein-midje "2.0.4"]]}})
