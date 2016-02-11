describe( "GithubCtrl", function() {
    var scope = undefined;
    var ctrl = undefined;
    var gh  = undefined;
    var ghs = undefined;
    var repo = undefined;
    var geo = undefined;

    function generateMockGeolocationSupport( lat, lng ) {
        response = ( lat && lng ) ? { coords: { lat: lat, lng: lng } } : { coords: CITIES[0] };
        geo = { getCurrentPosition: function( success, failure ) {
            success( response );
        } };
        spyOn( geo, "getCurrentPosition" ).and.callThrough();
    }

    var PR_ID = 12345;
    function generateMockRepositorySupport() {
        repo = { 
            fork: function( cb ) {
                cb( false );
            },
            write: function( branch, filename, data, commit_msg, cb ) {
                cb( false );
            },
            createPullRequest: function( pull, cb ) {
                cb( false, PR_ID );
            },
            read: function( branch, filename, cb ) {
                cb( undefined, JSON.stringify( filename == "cities.json" ? CITIES : PORTLAND ) );
            } 
        };
        spyOn( repo, "fork" ).and.callThrough();
        spyOn( repo, "write" ).and.callThrough();
        spyOn( repo, "createPullRequest" ).and.callThrough();
        spyOn( repo, "read" ).and.callThrough();

        gh = { getRepo: function() {} };
        spyOn( gh, "getRepo" ).and.callFake( function() {
            return repo;
        } );
        ghs = { create: function() { return gh; } };
    }

    var mockFirebase = mockSimpleLogin = undefined;
    function generateMockFirebaseSupport() {
        mockFirebase = function() {};
        mockSimpleLogin = function() {
            return { 
                '$login': function() {
                    return { then: function( cb ) {
                        cb( { name: "someUser",
                              accessToken: "abcdefghi" } );

                    } };
                }
            }
        };
    }

    beforeEach( module( "coffeetech" ) );

    beforeEach( inject( function ($controller, $rootScope ) {
        generateMockGeolocationSupport();
        generateMockRepositorySupport();
        generateMockFirebaseSupport();
        scope = $rootScope.$new();
        ctrl = $controller( "GithubCtrl", { $scope: scope, Github: ghs, Geo: geo, '$firebase': mockFirebase, '$firebaseSimpleLogin': mockSimpleLogin } );
    } ) );

    describe( "#init", function() {
        it( "should initialize, grabbing current city", function() {
            scope.init();
            expect( geo.getCurrentPosition ).toHaveBeenCalled();
            expect( gh.getRepo ).toHaveBeenCalled();
            expect( repo.read ).toHaveBeenCalled();
            expect( scope.cities.length ).toEqual( 2 );
            expect( scope.city.name ).toEqual( "portland" );
            expect( scope.shops.length ).toEqual( 3 );
        });
    });

    describe( "#calculateDistance", function() {
        it( "should find distance between two points", function() {
            expect( parseInt( scope.calculateDistance( 14.599512, 120.98422, 10.315699, 123.885437 ) * 0.621371 ) ).toEqual( 354 );
        });
    });

});
