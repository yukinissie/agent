(ns cca-mcp.core
  (:require
   [cca-mcp.tools.cheer :refer [cheer]]
   [cca-mcp.specs :as specs]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.util.response :as response]
   [farseer.handler :as f])
  (:gen-class))

(def server-info
  {:name "cca-mcp-server"
   :version "1.0.0"})

(def tools
  [{:name "cheer"
    :description "A tool that cheers on your coding with full enthusiasm"}])

(defn rpc-initialize
  [context _]
  {:protocolVersion "2024-11-05"
   :capabilities {:tools {:listChanged true}}
   :serverInfo server-info})

(defn rpc-tools-list
  [context _]
  {:tools tools})

(defn rpc-ping
  [context _]
  {})

(defn rpc-tools-call
  [context {:keys [name] :as params}]
  (case name
    "cheer" (cheer {:params params})
    (throw (ex-info (str "Tool '" name "' not found")
                    {:type :method-not-found}))))

(def rpc-config
  {:rpc/handlers
   {:initialize
    {:handler/function #'rpc-initialize
     :handler/spec-out ::specs/initialize-result}

    :tools/list
    {:handler/function #'rpc-tools-list
     :handler/spec-out ::specs/tools-list-result}

    :ping
    {:handler/function #'rpc-ping
     :handler/spec-out ::specs/ping-result}

    :tools/call
    {:handler/function #'rpc-tools-call
     :handler/spec-in ::specs/tool-call-params
     :handler/spec-out ::specs/tools-call-result}}})

(def rpc-handler (f/make-handler rpc-config))

(defn mcp-handler [req]
  (if (= (:request-method req) :post)
    (let [request (:body req)
          response (rpc-handler request)]
      (-> (response/response response)
          (response/content-type "application/json")))
    (-> (response/response {:jsonrpc "2.0"
                           :id nil
                           :error {:code -32600
                                   :message "Only POST method allowed"}})
        (response/status 405)
        (response/content-type "application/json"))))

(def app
  (-> mcp-handler
      (wrap-json-body
       {:keywords? true :malformed-response
        (fn [request]
          (-> (response/response
               {:jsonrpc "2.0"
                :id nil
                :error {:code -32700
                        :message "Invalid JSON"}})
              (response/status 400)
              (response/content-type "application/json")))})
      wrap-json-response))

(defn -main
  "Start the HTTP MCP server"
  [& args]
  (let [port (Integer/parseInt (or (first args) "4000"))]
    (println (str "Starting CCA MCP HTTP Server on port " port "..."))
    (jetty/run-jetty app {:port port :join? true})))
