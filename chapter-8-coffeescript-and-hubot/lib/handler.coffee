_SECRET = undefined
crypto = require 'crypto'
_API_TOKEN = undefined

flattenUsers = (users) ->
        rv = []
        for x in Object.keys( users )
               rv.push users[x]
        rv 

anyoneButProbot = ( users ) ->
        user = undefined
        flattened = flattenUsers( users )
        while not user
                user = flattened[ parseInt( Math.random() * flattened.length ) ].name
                user = undefined if "probot" == user
        user

sendPrRequest = ( robot, users, room, url, number ) ->
        user = anyoneButProbot( users )
        robot.messageRoom room, "#{user}: Hey, want a PR? #{url}. Enter 'accept #{number}' to accept the PR."
        pr = exports.decodePullRequest( url )
        robot.brain.set( pr.number, url )

exports.getSecureHash = ( body ) ->
        hmac = crypto.createHmac( 'sha1', _SECRET )
        hmac.setEncoding( 'hex' )
        hmac.write( body )
        hmac.end()
        hash = hmac.read()
        hash

exports.prHandler = ( robot, req, res ) ->
        
        rawBody = req.rawBody
        body = rawBody.split( '=' ) if rawBody
        payloadData = body[1] if body and body.length == 2
        if payloadData
                decodedJson = decodeURIComponent payloadData
                pr = JSON.parse decodedJson
                
                if pr and pr.pull_request
                        url = pr.pull_request.html_url
                        number = pr.pull_request.number
                        secureHash = exports.getSecureHash( rawBody )
                        signatureKey = "x-hub-signature"
                        webhookProvidedHash = req.headers[ signatureKey ] if req?.headers
                        secureCompare = require 'secure-compare'
                        if secureCompare( "sha1=#{secureHash}", webhookProvidedHash ) and url
                                room = "general"
                                users = robot.brain.users()
                                sendPrRequest( robot, users, room, url, number )
                        else
                                a = 1 # null-op 
                else
                        console.log "No pull request in here"
                        
        res.send "OK\n"

_GITHUB = undefined
_PR_URL = undefined

exports.decodePullRequest = (url) ->
        rv = {}
        if url
                chunks = url.split "/"
                if chunks.length == 7
                        rv.user = chunks[3]
                        rv.repo = chunks[4]
                        rv.number = chunks[6]
        rv

exports.getUsernameFromResponse = ( res ) ->
        res.message.user.name

exports.accept = ( robot, res ) ->

        prNumber = res.match[1]
        url = robot.brain.get( prNumber )

        msg = exports.decodePullRequest( url )
        username = exports.getUsernameFromResponse( res )
        msg.collabuser = username

        _GITHUB.repos.getCollaborator msg, ( err, collaborator ) ->
                msg.body = "@#{username} will review this (via Probot)."
                
                _GITHUB.issues.createComment msg, ( err, data ) ->
                        unless err
                                res.reply "Thanks, I've noted that in a PR comment. Review the PR here: "
                        else
                                res.reply "Something went wrong, I could not tag you on the PR comment: #{require('util').inspect( err )}"
                
exports.decline = ( res ) ->
        res.reply "No problem, we'll go through this PR in a bug scrub"

exports.setApiToken = (github, token) ->
        _API_TOKEN = token
        _GITHUB = github
        _GITHUB.authenticate type: "oauth", token: token

exports.setSecret = (secret) ->
        _SECRET = secret
