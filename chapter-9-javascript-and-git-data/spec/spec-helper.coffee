request = require "request"

class Requester
  get: (path, callback) ->
    request "http://localhost:3000#{path}", callback

  post: (path, body, callback) ->
    request.post {url: "http://localhost:3000#{path}", body: body}, callback

exports.withServer = (callback) ->
  asyncSpecWait()

  {app} = require "../github-local-login"

  stopServer = ->
    app.close()
    asyncSpecDone()

  app.listen 3000

  callback new Requester, stopServer
