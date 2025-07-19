# method: tool/call

## ユーザーはテストが通過すると Agent からの歓声を受けることができる
* URL"/"にボディ<file:fixtures/call/body.json>で、POSTリクエストを送る
* レスポンスステータスコードが"200"である
* レスポンスのJSONの"$.result.content[0].text"が文字列の"うをおぉぉぉぉぉぉおおおっ！！！"である