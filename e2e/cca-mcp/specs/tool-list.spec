# /tools/list

## ツールリストを得ることができる
* URL"/"にボディ<file:fixtures/tool-list/body.json>で、POSTリクエストを送る
* レスポンスステータスコードが"200"である
* レスポンスのJSONの"$.result.tools.[0].name"が文字列の"cheer"である
* レスポンスのJSONの"$.result.tools.[0].description"が文字列の"A tool that cheers on your coding with full enthusiasm"である
