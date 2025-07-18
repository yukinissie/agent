(ns agent-api.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route])
  (:gen-class))

(defn health-handler [request]
  (response {:status "ok" :message "Agent API is running"}))

(defn agents-handler [request]
  (response [{:id 1 :name "Agent Smith" :type "AI Assistant"}
             {:id 2 :name "Agent Jones" :type "Data Collector"}]))

(defn agent-handler [request]
  (let [id (-> request :params :id)]
    (response {:id (Integer/parseInt id) 
               :name (str "Agent " id) 
               :type "Dynamic Agent"})))

(defroutes app-routes
  (GET "/health" [] health-handler)
  (GET "/agents" [] agents-handler)
  (GET "/agents/:id" [] agent-handler)
  (route/not-found {:error "Not Found"}))

(def app
  (-> app-routes
      wrap-json-response
      (wrap-json-body {:keywords? true :bigdecimals? true})))

(defn -main [& args]
  (println "Starting Agent API server on port 3000...")
  (run-jetty app {:port 3000 :join? true}))
