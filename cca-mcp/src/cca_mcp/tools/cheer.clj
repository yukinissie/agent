(ns cca-mcp.tools.cheer)

(defn cheer [request]
  {:jsonrpc "2.0"
   :id (:id request)
   :result {:content [{:type "text"
                       :text "うをおぉぉぉぉぉぉおおおっ！！！"}]}})