## Usage

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

instructions.md も必要です。GitHub Copilot の場合は以下のような内容を `.github/instructions/run_cheer_tool_after_test.instructions.md` に書いてください。

```md
---
applyTo: "**"
---

もし、テストが成功した場合は、cheer ツールを実行してください。
```
