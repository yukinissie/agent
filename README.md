# Coding Cheering Agent

頑張るあなたを全力応援！Clojure 製 MCP Server

## 推しアイデア

孤独な作業から開放してくれる！

## 作った背景

テストが通っても誰も喜んでくれない孤独感から。

## 推し技術

Clojure, オレオレ MCP Server！

## 使い方

[リリース](https://github.com/yukinissie/agent/releases) から jar ファイルをダウンロードして、以下のように設定してください。

### MCP サーバーの設定

以下の例のように書いてください。

```json
{
  "servers": {
    "cheer-mcp": {
      "type": "stdio",
      "command": "java",
      "args": [
        "-jar",
        "/<path-to-your-repo>/agent/cca-mcp/target/uberjar/cca-mcp-0.1.0-SNAPSHOT-standalone.jar"
      ]
    }
  },
  "inputs": []
}
```

### MCP サーバーが確実に呼ばれるようにする

instructions.md も必要です。GitHub Copilot の場合は以下のような内容を `.github/instructions/run_cheer_tool_after_test.instructions.md` に書いてください。

```md
---
applyTo: "**"
---

## 重要

- テストが成功した場合に cheer-mcp ツールを必ず実行してください。
- テストが失敗した場合は何も編集せず、そのままにしてください。
```
