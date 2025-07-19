(ns cca-mcp.core
  (:require
   [cca-mcp.tools.cheer :refer [cheer]]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.util.response :as response])
  (:gen-class))

(def server-info
  {:name "cca-mcp-server"
   :version "1.0.0"})

(def tools
  [{:name "cheer"
    :description "A tool that cheers on your coding with full enthusiasm"}])

(defmulti handle-request :method)

(defmethod handle-request "initialize" [request]
  {:jsonrpc "2.0"
   :id (:id request)
   :result {:protocolVersion "2024-11-05"
            :capabilities {:tools {:listChanged true}}
            :serverInfo server-info}})

(defmethod handle-request "tools/list" [request]
  {:jsonrpc "2.0"
   :id (:id request)
   :result {:tools tools}})

(defmethod handle-request "ping" [request]
  {:jsonrpc "2.0"
   :id (:id request)
   :result {}})

(defmethod handle-request "tools/call" [request]
  (let [tool-name (get-in request [:params :name])]
    (case tool-name
      "cheer" (cheer request)
      {:jsonrpc "2.0"
       :id (:id request)
       :error {:code -32601
               :message "Method not found"}})))

(defmethod handle-request :default [request]
  {:jsonrpc "2.0"
   :id (:id request)
   :error {:code -32601
           :message "Method not found"}})

(defn mcp-handler [req]
  (try
    (if (= (:request-method req) :post)
      (let [request (:body req)
            response (handle-request request)]
        (-> (response/response response)
            (response/content-type "application/json")))
      (-> (response/response {:error "Only POST method allowed"})
          (response/status 405)
          (response/content-type "application/json")))
    (catch Exception e
      (let [error-response {:jsonrpc "2.0"
                            :error {:code -32700
                                    :message "Parse error"}}]
        (-> (response/response error-response)
            (response/status 400)
            (response/content-type "application/json"))))))

(def app
  (-> mcp-handler
      (wrap-json-body {:keywords? true})
      wrap-json-response))

(defn -main
  "Start the HTTP MCP server"
  [& args]
  (let [port (Integer/parseInt (or (first args) "4000"))]
    (println (str "Starting CCA MCP HTTP Server on port " port "..."))
    (jetty/run-jetty app {:port port :join? true})))
