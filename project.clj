(defproject rm-hull/wireframes "0.0.1-SNAPSHOT"
  :description "A lightweight 3D wireframe renderer for both Clojure and ClojureScript"
  :url "https://github.com/rm-hull/wireframes"
  :license {:name "The MIT License (MIT)"
            :url "http://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2156"]
                 [org.clojure/core.rrb-vector "0.0.10"]
                 [gloss "0.2.2"]
                 [potemkin "0.3.4"]
                 [byte-streams "0.1.9"]
                 [com.taoensso/timbre "2.7.1"]
                 [cljs-webgl "0.1.4-SNAPSHOT"]
                 [rm-hull/dommy "0.1.3-SNAPSHOT"]
                 [hiccup "1.0.5"]
                 [jayq "2.5.0"]
                 [rm-hull/monet "0.1.10"]
                 [rm-hull/inkspot "0.0.1-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.0.2"]
            [com.birdseye-sw/lein-dalap "0.1.0"]]
  :hooks [leiningen.dalap
          leiningen.cljsbuild]
  :source-paths ["src"]
  :cljsbuild {
    :repl-listen-port 9000
    :repl-launch-commands
      {"firefox" ["firefox"]
       "firefox-demo" ["firefox" "doc/gallery/cljs-demo/gallery.html"]}
    :builds {
      :main {
        :source-paths ["target/generated-src"]
        :jar true
        :compiler {
          :output-to "target/wireframes.js"
          :source-map "target/wireframes.map"
          :static-fns true
          ;:optimizations :advanced
          :pretty-print true
          :externs ["resources/private/externs/jquery.js"] }}}}
  :test-selectors {:default (complement :examples)
                   :examples :examples }
  :min-lein-version "2.3.2"
  :global-vars {*warn-on-reflection* true}
  :repositories {"sonartype snapshots" "https://oss.sonatype.org/content/repositories/snapshots"})
