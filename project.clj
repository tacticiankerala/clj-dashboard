(defproject project-health-dashboard "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3297"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [google-apps-clj "0.2.1"]
                 [sablono "0.3.4"]
                 [org.omcljs/om "0.8.8"]
                 [com.taoensso/sente "1.6.0"]
                 [compojure "1.4.0"]
                 [ring "1.4.0"]
                 [racehub/om-bootstrap "0.5.0"]
                 [prismatic/om-tools "0.3.12"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.5"]
            [lein-autoreload "0.1.0"]
            [lein-npm "0.6.1"]]

  :npm {:dependencies [[react-grid-layout "0.8.5"]]} 

  :repl-options {:init-ns project-health-dashboard.core }

  :source-paths ["src/clj/"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src/cljs/"]

              :figwheel { :on-jsload "project-health-dashboard.core/on-js-reload" }

              :compiler {:main project-health-dashboard.core
                         :asset-path "js/compiled/out"
                         :output-to "resources/public/js/compiled/project_health_dashboard.js"
                         :output-dir "resources/public/js/compiled/out"
                         :source-map-timestamp true }}
             {:id "min"
              :source-paths ["src/cljs/"]
              :compiler {:output-to "resources/public/js/compiled/project_health_dashboard.js"
                         :main project-health-dashboard.core
                         :optimizations :advanced
                         :pretty-print false}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources" 
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1" 

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             ;; :nrepl-port 7888

             :ring-handler project-health-dashboard.core/my-app
             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler

             ;; To be able to open files in your editor from the heads up display
             ;; you will need to put a script on your path.
             ;; that script will have to take a file path and a line number
             ;; ie. in  ~/bin/myfile-opener
             ;; #! /bin/sh
             ;; emacsclient -n +$2 $1
             ;;
             ;; :open-file-command "myfile-opener"

             ;; if you want to disable the REPL
             ;; :repl false

             ;; to configure a different figwheel logfile path
             ;; :server-logfile "tmp/logs/figwheel-logfile.log" 
             })
