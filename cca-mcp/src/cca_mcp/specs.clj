(ns cca-mcp.specs
  (:require [clojure.spec.alpha :as s]))

;; ===== JSON-RPC 2.0 Base Specs =====
(s/def ::jsonrpc #{"2.0"})
(s/def ::method string?)
(s/def ::params (s/nilable map?))
(s/def ::id (s/nilable (s/or :string string? :number number?)))

(s/def ::jsonrpc-request
  (s/keys :req-un [::jsonrpc ::method]
          :opt-un [::params ::id]))

;; Error Response
(s/def ::error-code int?)
(s/def ::error-message string?)
(s/def ::error-data any?)
(s/def ::rpc-error
  (s/keys :req-un [::error-code ::error-message]
          :opt-un [::error-data]))

(s/def ::result any?)
(s/def ::jsonrpc-response
  (s/and (s/keys :req-un [::jsonrpc ::id]
                 :opt-un [::result ::rpc-error])
         #(or (contains? % :result) (contains? % :rpc-error))))

;; ===== MCP Common Specs =====
(s/def ::name string?)
(s/def ::version string?)
(s/def ::description string?)
(s/def ::protocolVersion #{"2024-11-05"})

;; Server Info
(s/def ::serverInfo
  (s/keys :req-un [::name ::version]))

;; Capabilities
(s/def ::listChanged boolean?)
(s/def ::tools-capability 
  (s/keys :opt-un [::listChanged]))
(s/def ::tools-enabled boolean?)
(s/def ::capabilities 
  (s/keys :opt-un [::tools-enabled]))

;; ===== Tool Specs =====
(s/def ::tool
  (s/keys :req-un [::name ::description]))
(s/def ::tools (s/coll-of ::tool))

(s/def ::arguments (s/nilable map?))
(s/def ::tool-call-params
  (s/keys :req-un [::name] :opt-un [::arguments]))

;; Tool Response Content
(s/def ::type #{"text" "image" "resource"})
(s/def ::text string?)
(s/def ::content-item
  (s/keys :req-un [::type ::text]))
(s/def ::content (s/coll-of ::content-item))
(s/def ::isError boolean?)


;; Initialize
(s/def ::initialize-result
  (s/keys :req-un [::protocolVersion ::capabilities ::serverInfo]))

;; Tools List
(s/def ::tools-list-result
  (s/keys :req-un [::tools]))

;; Tools Call
(s/def ::tools-call-result
  (s/keys :req-un [::content] :opt-un [::isError]))

;; Ping
(s/def ::ping-result map?)

;; ===== Method-specific Request Validation =====
(s/def ::initialize-request
  (s/and ::jsonrpc-request
         #(= "initialize" (:method %))))

(s/def ::tools-list-request
  (s/and ::jsonrpc-request
         #(= "tools/list" (:method %))))

(s/def ::tools-call-request
  (s/and ::jsonrpc-request
         #(= "tools/call" (:method %))
         #(s/valid? ::tool-call-params (:params %))))

(s/def ::ping-request
  (s/and ::jsonrpc-request
         #(= "ping" (:method %))))