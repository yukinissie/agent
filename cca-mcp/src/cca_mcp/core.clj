(ns cca-mcp.core
  (:require
   [cca-mcp.tools.cheer :refer [cheer]]
   [ring.adapter.jetty :as jetty]
   [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
   [ring.util.response :as response]
   [clojure.spec.alpha :as s])
  (:gen-class))

(def server-info
  {:name "cca-mcp-server"
   :version "1.0.0"})

(def tools
  [{:name "cheer"
    :description "A tool that cheers on your coding with full enthusiasm"}])

(def ^:const error-codes
  {:parse-error      -32700
   :invalid-request  -32600
   :method-not-found -32601
   :invalid-params   -32602
   :internal-error   -32603})

(def ^:const error-messages
  {:parse-error      "Parse error"
   :invalid-request  "Invalid Request"
   :method-not-found "Method not found"
   :invalid-params   "Invalid params"
   :internal-error   "Internal error"})

(s/def ::jsonrpc #(= "2.0" %))
(s/def ::id (s/or :number number? :string string? :null nil?))
(s/def ::method string?)
(s/def ::params (s/or :array vector? :object map?))

(s/def ::jsonrpc-request
  (s/keys :req-un [::jsonrpc ::method]
          :opt-un [::id ::params]))

(defn make-error
  ([error-type]
   (make-error error-type nil))
  ([error-type data]
   (cond-> {:code (error-codes error-type)
            :message (error-messages error-type)}
     data (assoc :data data))))

(defn error-response [request-id error-type & [data]]
  {:jsonrpc "2.0"
   :id request-id
   :error (make-error error-type data)})

(defn success-response [request-id result]
  {:jsonrpc "2.0"
   :id request-id
   :result result})

(defn validate-request [request]
  (when-not (s/valid? ::jsonrpc-request request)
    (let [explain-data (s/explain-data ::jsonrpc-request request)]
      (throw (ex-info "Invalid request" {:type :invalid-request
                                         :explain-data explain-data})))))

(defmulti handle-request :method)

(defmethod handle-request "initialize" [request]
  (try
    (success-response (:id request)
                      {:protocolVersion "2024-11-05"
                       :capabilities {:tools {:listChanged true}}
                       :serverInfo server-info})
    (catch Exception e
      (error-response (:id request) :internal-error (.getMessage e)))))

(defmethod handle-request "tools/list" [request]
  (try
    (success-response (:id request) {:tools tools})
    (catch Exception e
      (error-response (:id request) :internal-error (.getMessage e)))))

(defmethod handle-request "ping" [request]
  (try
    (success-response (:id request) {})
    (catch Exception e
      (error-response (:id request) :internal-error (.getMessage e)))))

(defmethod handle-request "tools/call" [{:keys [params] :as request}]
  (try
    (let [{:keys [name]} params]
      (when-not name
        (throw (ex-info "Missing tool name" {:type :invalid-params})))
      (case name
        "cheer" (cheer request)
        (error-response (:id request) :method-not-found
                        (str "Tool '" name "' not found"))))
    (catch clojure.lang.ExceptionInfo e
      (let [data (ex-data e)]
        (case (:type data)
          :invalid-params (error-response (:id request) :invalid-params (.getMessage e))
          (error-response (:id request) :internal-error (.getMessage e)))))
    (catch Exception e
      (error-response (:id request) :internal-error (.getMessage e)))))

(defmethod handle-request :default [request]
  (error-response (:id request) :method-not-found
                  (str "Method '" (:method request) "' not found")))

(defn mcp-handler [req]
  (try
    (if (= (:request-method req) :post)
      (let [request (:body req)]
        (try
          (validate-request request)
          (let [response (handle-request request)]
            (-> (response/response response)
                (response/content-type "application/json")))
          (catch clojure.lang.ExceptionInfo e
            (let [data (ex-data e)
                  error-resp (case (:type data)
                               :invalid-request (error-response (:id request) :invalid-request
                                                                 (:explain-data data))
                               (error-response (:id request) :internal-error (.getMessage e)))]
              (-> (response/response error-resp)
                  (response/status 400)
                  (response/content-type "application/json"))))
          (catch Exception e
            (let [error-resp (error-response nil :internal-error (.getMessage e))]
              (-> (response/response error-resp)
                  (response/status 500)
                  (response/content-type "application/json"))))))
      (-> (response/response (error-response nil :invalid-request "Only POST method allowed"))
          (response/status 405)
          (response/content-type "application/json")))
    (catch com.fasterxml.jackson.core.JsonParseException e
      (let [error-resp (error-response nil :parse-error (.getMessage e))]
        (-> (response/response error-resp)
            (response/status 400)
            (response/content-type "application/json"))))
    (catch Exception e
      (let [error-resp (error-response nil :parse-error "Invalid JSON")]
        (-> (response/response error-resp)
            (response/status 400)
            (response/content-type "application/json"))))))

(def app
  (-> mcp-handler
      (wrap-json-body {:keywords? true :malformed-response (fn [request]
                                                             (-> (response/response (error-response nil :parse-error "Invalid JSON"))
                                                                 (response/status 400)
                                                                 (response/content-type "application/json")))})
      wrap-json-response))

(defn -main
  "Start the HTTP MCP server"
  [& args]
  (let [port (Integer/parseInt (or (first args) "4000"))]
    (println (str "Starting CCA MCP HTTP Server on port " port "..."))
    (jetty/run-jetty app {:port port :join? true})))
