# 不正なリクエストをした場合

## 定義されていないメソッドでリクエストした場合、Method not foundエラーが返る
* URL"/"にボディ<file:fixtures/error/method-not-found.json>で、POSTリクエストを送る
* レスポンスステータスコードが"200"である
* レスポンスのJSONの"$.error.code"が整数の"-32601"である
* レスポンスのJSONの"$.error.message"が文字列の"Method not found"である

## POST以外でリクエストされた場合は、405エラーが返る
* URL"/"にGETリクエストを送る
* レスポンスステータスコードが"405"である
