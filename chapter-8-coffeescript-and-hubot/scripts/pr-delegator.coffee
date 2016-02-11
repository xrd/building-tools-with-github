handler = require '../lib/handler'

handler.setSecret process.env.PROBOT_SECRET 
github = require 'github'
ginst = new github version: '3.0.0'
handler.setApiToken ginst, process.env.PROBOT_API_TOKEN 

module.exports = (robot) ->

        robot.respond /accept (.*)/i, ( res ) ->
                handler.accept( robot, res )

        robot.respond /decline/i, ( res ) ->
                handler.decline( res )

        robot.router.post '/pr', ( req, res ) ->
                handler.prHandler( robot, req, res )
