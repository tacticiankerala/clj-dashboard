(ns ^:figwheel-always project-health-dashboard.core
    (:require[om.core :as om :include-macros true]
             [om.dom :as dom :include-macros true]
             [taoensso.sente  :as sente :refer (cb-success?)]
             [taoensso.encore :as enc    :refer (tracef debugf infof warnf errorf)]
             [om-bootstrap.table :refer [table]]
             [om-tools.dom :as d :include-macros true]))

(enable-console-print!)



(defonce app-state (atom {:headers [ "metric1" "metric2"]}))


(defn the-table [data]
  (table {:striped? true :bordered? true :condensed? true :hover? true}
       (d/thead
        (d/tr
         (map d/th (:headers data))))
       (d/tbody
        (map #(d/tr (map d/td %)) (:values data)))))

(om/root
  (fn [data owner]
    (reify om/IRender
      (render [_]
        (the-table data))))
  app-state
  {:target (. js/document (getElementById "app"))})





(let [{:keys [chsk ch-recv send-fn state]}
      (sente/make-channel-socket! "/chsk" ; Note the same path as before
       {:type :auto ; e/o #{:auto :ajax :ws}
       })]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )


(defmulti event-msg-handler :id) ; Dispatch on event-id
    ;; Wrap for logging, catching, etc.:
    (defn     event-msg-handler* [{:as ev-msg :keys [id ?data event]}]
      (debugf "Event: %s" event)
      (event-msg-handler ev-msg))


    (do ; Client-side methods
      (defmethod event-msg-handler :default ; Fallback
        [{:as ev-msg :keys [event]}]
        (debugf "Unhandled event: %s" event))
      
      (defmethod event-msg-handler :chsk/state
        [{:as ev-msg :keys [?data]}]
        (if (= ?data {:first-open? true})
          (debugf "Channel socket successfully established!")
          (debugf "Channel socket state change: %s" ?data)))
      
      (defmethod event-msg-handler :chsk/recv
        [{:as ev-msg :keys [?data]}]
        (do
          (let [[_ message] ?data]
            (debugf "Push event from server: %s" ?data)
            (reset! app-state (:data message)))))
      
      (defmethod event-msg-handler :chsk/handshake
        [{:as ev-msg :keys [?data]}]
        (let [[?uid ?csrf-token ?handshake-data] ?data]
          (debugf "Handshake: %s" ?data)))
      
      ;; Add your (defmethod handle-event-msg! <event-id> [ev-msg] <body>)s here...
      )


(def router_ (atom nil))
    (defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
    (defn start-router! []
      (stop-router!)
      (reset! router_ (sente/start-chsk-router! ch-chsk event-msg-handler*)))


    (defn start! []
      (start-router!))
    
    (start!)


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)

