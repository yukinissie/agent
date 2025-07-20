(ns cca-mcp.tools.cheer
  (:require [clojure.java.shell :as shell]))

(defn cheer [request]
  (let [cheerText "うをおぉぉぉぉぉぉおおおっ！！！"]
    (when (= "Mac OS X" (System/getProperty "os.name"))
      (shell/sh "say" cheerText))
    {:content [{:type "text"
                :text cheerText}]}))