
Probot = require "../scripts/pr-delegator"
Handler = require "../lib/handler"

pr = undefined
robot = undefined

describe "#probot", ->
        beforeEach () ->
                robot = {
                        respond: jasmine.createSpy( 'respond' )
                        router: {
                                post: jasmine.createSpy( 'post' )
                                }
                        }

        it "should verify our calls to respond", (done) ->
                pr = Probot robot
                expect( robot.respond.calls.count() ).toEqual( 2 )
                done()

        it "should verify our calls to router.post", (done) ->
                pr = Probot robot
                expect( robot.router.post ).toHaveBeenCalled()
                done()

        describe "#pr", ->
                secret = "ABCDEF"
                robot = undefined
                res = undefined
                json = '{ "members" : [ { "name" : "bar" } , { "name" : "foo" } ] }'
                httpSpy = jasmine.createSpy( 'http' ).and.returnValue(
                        { get: () -> ( func ) ->
                                func( undefined, undefined, json ) } )

                users = { CDAWSON: { name: "Chris Dawson" }, BSTRAUB: { name: "Ben Straub" } }
                brainSpy = {
                        users: jasmine.createSpy( 'getUsers' ).and.returnValue( users ),
                        set: jasmine.createSpy( 'setBrain' ),
                        get: jasmine.createSpy( 'getBrain' ).and.returnValue( "https://github.com/xrd/testing_repository/pull/1" )
                        }
                
                beforeEach ->
                        robot = {
                                messageRoom: jasmine.createSpy( 'messageRoom' )
                                http: httpSpy
                                brain: brainSpy
                                }
                                
                        res = { send: jasmine.createSpy( 'send' ) }
                        Handler.setSecret secret
                
                it "should disallow calls without the secret and url", (done) ->
                        req = {}
                        Handler.prHandler( robot, req, res )
                        expect( robot.messageRoom ).not.toHaveBeenCalled()
                        expect( httpSpy ).not.toHaveBeenCalled()
                        expect( res.send ).toHaveBeenCalled()
                        done()

                it "should allow calls with the secret and url", (done) ->
                        payload =  '{ "pull_request" : { "html_url" : "https://github.com/xrd/testing_repository/pull/1" } }'
                        bodyPayload = "payload=#{encodeURIComponent(payload)}"
                        payloadSignature = Handler.getSecureHash( bodyPayload )
                        req = { rawBody: bodyPayload,
                        headers: { "x-hub-signature" : "sha1=#{payloadSignature}" } }

                        Handler.prHandler( robot, req, res )
                        expect( robot.messageRoom ).toHaveBeenCalled()
                        expect( res.send ).toHaveBeenCalled()
                        done()

                describe "#response", ->
                        createComment = jasmine.createSpy( 'createComment' ).and.
                                callFake( ( msg, cb ) -> cb( false, "some data" ) )
                        issues = { createComment: createComment }
                        authenticate = jasmine.createSpy( 'ghAuthenticate' )
                        responder = { reply: jasmine.createSpy( 'reply' ),
                        match: [ undefined, "1" ],
                        send: jasmine.createSpy( 'send' ),
                        message: { user: { name: "Chris Dawson" } } }
                        getCollaborator = jasmine.createSpy( 'getCollaborator' ).and.
                                callFake( ( msg, cb ) -> cb( false, true ) )
                        repos = { getCollaborator: getCollaborator }

                        beforeEach ->
                                githubBinding = { authenticate: authenticate, issues: issues, repos: repos }
                                github = Handler.setApiToken( githubBinding, "ABCDEF" )
                                payload =  '{ "pull_request" : { "html_url" : "http://pr/1" } }'
                                bodyPayload = "payload=#{encodeURIComponent(payload)}"
                                req = { rawBody: bodyPayload,
                                headers: { "x-hub-signature" : "sha1=dc827de09c5b57da3ee54dcfc8c5d09a3d3e6109" } }
                                Handler.prHandler( robot, req, responder )

                        it "should tag the PR on GitHub if the user accepts", (done) ->
                                Handler.accept( robot, responder )
                                expect( authenticate ).toHaveBeenCalled()
                                expect( createComment ).toHaveBeenCalled() 
                                expect( responder.reply ).toHaveBeenCalled()
                                expect( repos.getCollaborator ).toHaveBeenCalled()
                                done()

                        it "should not tag the PR on GitHub if the user declines", (done) ->
                                Handler.decline( responder )
                                expect( authenticate ).toHaveBeenCalled()
                                expect( createComment ).not.toHaveBeenCalledWith()
                                expect( responder.reply ).toHaveBeenCalled()
                                done()

                        it "should decode the URL into a proper message object for the createMessage call", (done) ->
                                url = "https://github.com/xrd/testing_repository/pull/1"
                                msg = Handler.decodePullRequest( url )
                                expect( msg.user ).toEqual( "xrd" )
                                expect( msg.repo ).toEqual( "testing_repository" )
                                expect( msg.number ).toEqual( "1" )
                                done()
                                
                        it "should get the username from the response object", (done) ->
                                expect( Handler.getUsernameFromResponse( responder ) ).toEqual "Chris Dawson"
                                done()



        
