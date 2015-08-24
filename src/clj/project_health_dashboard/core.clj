(ns project-health-dashboard.core
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [google-apps-clj.credentials :as google-creds]
            [google-apps-clj.google-sheets :as sheets]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.params]
            [ring.middleware.keyword-params]
            [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop thread alts! timeout)]
            ))

(defn fetch-data
  [spread-sheet-name worksheet-name]
  (let [creds (edn/read-string (slurp "config/goole-creds.edn"))
        sheet-service (sheets/build-sheet-service creds)
        entry (sheets/find-spreadsheet-by-id sheet-service spread-sheet-name)
        worksheet (sheets/find-worksheet-by-title sheet-service (:spreadsheet entry) worksheet-name)]
    {:headers  (filterv (complement str/blank?) (sheets/read-worksheet-headers sheet-service (:worksheet worksheet)))
     :values  (into [] (sheets/read-worksheet-values sheet-service (:worksheet worksheet)))}))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]}
      (sente/make-channel-socket! sente-web-server-adapter {})]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )


(defn start-broadcaster! []
  (go-loop []
    (<!! (async/timeout 500))
1    (println (format "Broadcasting server>user: %s" @connected-uids))
    (doseq [uid (:any @connected-uids)]
      (let [data-fetcher (future (try (fetch-data "1fWrzA1iWmjGDyY0Djol9_5NlwQP2S1BvvBtlm8HT-wA" "Sheet1")
                                      (catch Exception e (do (println (str "exception" (.getMessage e)))
                                                             {}))))
            data (deref data-fetcher 5000 nil)]
        (if data 
          (chsk-send! uid
                        [:project-dashboard/broadcast
                         {:what-is-this "Latest Project dashboard update"
                          :data  data }])
          (future-cancel data-fetcher))))
    (recur)))


(start-broadcaster!)

(defroutes my-app-routes
  (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
  (POST "/chsk" req (ring-ajax-post                req)))

(def my-app
  (-> my-app-routes
      ring.middleware.keyword-params/wrap-keyword-params
      ring.middleware.params/wrap-params))



