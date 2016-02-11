helper = require './spec-helper'

describe "App", ->
  describe "get /", ->
    it "responds successfully", ->
      helper.withServer (r, done) ->
        r.get "/token.json", (err, res, body) ->
          expect(res.statusCode).toEqual 200
          done()

  #   it "has the correct body", ->
  #     helper.withServer (r, done) ->
  #       r.get "/", (err, res, body) ->
  #         expect(body).toEqual "Hello, world!"
  #         done()

  # describe "post /", ->
  #   it "has the correct body", ->
  #     helper.withServer (r, done) ->
  #       r.post "/", "post body", (err, res, body) ->
  #         expect(body).toEqual "You posted!"
  #         done()
