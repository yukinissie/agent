(ns cca-mcp.handler.example
  (:require [ataraxy.core :as ataraxy]
            [ataraxy.response :as response]
            [integrant.core :as ig]))

(defmethod ig/init-key :cca-mcp.handler/example [_ options]
  (fn [{[_] :ataraxy/result}]
    [::response/ok "message: うをおぉぉぉぉおおおっ！！！"]))
