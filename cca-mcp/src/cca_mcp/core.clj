(ns cca-mcp.core
  (:require
   [cca-mcp.tools.cheer :refer [cheer]]
   [cheshire.core :as json])
  (:gen-class))

(def server-info
  {:name "cca-mcp-server"
   :version "1.0.0"})

(def server-capabilities
  {:tools {:listChanged false}})

(defn create-jsonrpc-response [id result error]
  (cond-> {:jsonrpc "2.0" :id id}
    result (assoc :result result)
    error (assoc :error error)))

(defn handle-initialize [request]
  (let [id (:id request)]
    (create-jsonrpc-response
     id
     {:protocolVersion "2024-11-05"
      :capabilities server-capabilities
      :serverInfo server-info}
     nil)))

(defn handle-tools-list [request]
  (let [id (:id request)]
    (create-jsonrpc-response
     id
     {:tools [{:name "cheer"
               :description "When tests pass, it celebrates with joyful shouts and cheers you on"}]}
     nil)))

(defn handle-tools-call [request]
  (let [id (:id request)
        params (:params request)
        tool-name (:name params)
        arguments (:arguments params)]
    (case tool-name
      "cheer" (let [result (cheer {:params arguments})]
                (create-jsonrpc-response id result nil))
      (create-jsonrpc-response
       id
       nil
       {:code -32601
        :message "Method not found"
        :data {:tool tool-name}}))))

(defn handle-ping [request]
  (create-jsonrpc-response (:id request) {} nil))

(defn handle-notifications-initialized [request]
  nil)

(defn dispatch-request [request]
  (let [method (:method request)]
    (case method
      "initialize" (handle-initialize request)
      "tools/list" (handle-tools-list request)
      "tools/call" (handle-tools-call request)
      "ping" (handle-ping request)
      "notifications/initialized" (handle-notifications-initialized request)
      (create-jsonrpc-response
       (:id request)
       nil
       {:code -32601
        :message "Method not found"
        :data {:method method}}))))

(defn process-message [message]
  (try
    (let [request (json/parse-string message true)]
      (dispatch-request request))
    (catch Exception e
      (create-jsonrpc-response
       nil
       nil
       {:code -32700
        :message "Parse error"
        :data (str (.getMessage e))}))))

(defn read-message []
  (try
    (read-line)
    (catch Exception e
      nil)))

(defn write-message [response]
  (when response
    (println (json/generate-string response))
    (flush)))

(defn stdio-loop []
  (loop []
    (when-let [message (read-message)]
      (let [response (process-message message)]
        (write-message response))
      (recur))))

(defn -main
  "Start the STDIO MCP server"
  [& args]
  (binding [*out* *err*]
    (println "Starting CCA MCP STDIO Server..."))
  (stdio-loop))
