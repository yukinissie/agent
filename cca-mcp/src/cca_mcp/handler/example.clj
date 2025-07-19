(ns cca-mcp.handler.example
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig])
  (:import [io.modelcontextprotocol.server McpServer]
           [io.modelcontextprotocol.server.transport StdioServerTransportProvider]))

(defmethod ig/init-key :cca-mcp.handler/example [_ options] 
  (McpServer/sync)
  (println "MCP Server started successfully!")
  (fn [{[_] :ataraxy/result}]
    [::response/ok "message: うをおぉぉぉぉおおおっ！！！"]))
