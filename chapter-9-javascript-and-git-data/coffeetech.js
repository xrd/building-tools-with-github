var mod = angular.module( 'coffeetech', [ 'firebase' ] );

mod.factory( 'Github', function() { 
    return { 
        create: function(token) { 
            return new Github( { token: token, auth: 'oauth' } );
        }
    };
});

mod.factory( 'Geo', [ '$window', function( $window ) { 
    return $window.navigator.geolocation;
} ] );

mod.controller( 'GithubCtrl', [ '$scope', 'Github', 'Geo', '$window', '$timeout', '$firebase', '$firebaseSimpleLogin', function( $scope, ghs, Geo, $window, $timeout, $firebase, $firebaseSimpleLogin ) {

    $scope.init = function() {
        
        var ref = new Firebase( 'https://coffeetech.firebaseio.com' );
        $scope.auth = $firebaseSimpleLogin( ref );
        
        $scope.getCurrentLocation( function( position ) {
            $scope.latitude = position.coords.latitude;
            $scope.longitude = position.coords.longitude;
            var gh = ghs.create();
            $scope.repo = gh.getRepo( "xrd", "spa.coffeete.ch" ); 
            $scope.repo.read( "gh-pages", "cities.json", function(err, data) { 
                $scope.cities = JSON.parse( data ); 
                // Determine our current city
                $scope.detectCurrentCity(); 

                // If we have a city, get it
                if( $scope.city ) {
                    $scope.retrieveCity();
                }

                $scope.$apply(); 
            });
        });
    };

    $scope.retrieveCity = function() {
        $scope.repo.read( "gh-pages", $scope.city.name + ".json", function(err, data) { 
            $scope.shops = JSON.parse( data );
            $scope.$apply();
        });
    }

    $scope.getCurrentLocation = function( cb ) {
        if( undefined != Geo ) {
            Geo.getCurrentPosition( cb, $scope.geolocationError );
        } else {
            console.error('not supported');
        }
        
    };

    $scope.detectCurrentCity = function() {
        // Calculate the distance from our current position and use
        // this to determine which city we are closest to and within
        // 25 miles
        for( var i = 0; i < $scope.cities.length; i++ ) {
            var dist = $scope.calculateDistance( $scope.latitude, $scope.longitude, $scope.cities[i].latitude, $scope.cities[i].longitude );
            if( dist < 25 ) {
                $scope.city = $scope.cities[i];
                break;
            }
        }
    }

    toRad = function(Value) {
        return Value * Math.PI / 180;
    };
    
    $scope.calculateDistance = function( latitude1, longitude1, latitude2, longitude2 ) {
        R = 6371;
        dLatitude = toRad(latitude2 - latitude1);
        dLongitude = toRad(longitude2 - longitude1);
        latitude1 = toRad(latitude1);
        latitude2 = toRad(latitude2);
        a = Math.sin(dLatitude / 2) * Math.sin(dLatitude / 2) + Math.sin(dLongitude / 2) * Math.sin(dLongitude / 2) * Math.cos(latitude1) * Math.cos(latitude2);
        c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        d = R * c;
        return d;
    }

    $scope.loadCity = function( city ) {
        $scope.repo.read( "gh-pages", city + ".json", function(err, data) { // # <2>
            $scope.shops = JSON.parse( data ); // # <3>
            $scope.$apply();
        });
    }
    
    $scope.geolocationError = function( error ) {
        console.log( "Inside failure" );
    };

    $scope.annotate = function( shop ) {
        $scope.shopToAnnotate = shop;

        $scope.auth.$login( 'github', { scope: 'repo' } ).then( function( user ) { // <1>

            $scope.me = user;
            $scope.username = user.username;

            $scope.annotation = $window.prompt( "Enter data to add" ); // <2>

            if( $scope.annotation ) {
                gh = ghs.create( $scope.me.accessToken ); // <3>
                toFork = gh.getRepo( "xrd", "spa.coffeete.ch" );
                toFork.fork( function( err ) {
                    if( !err ) {
                        $scope.notifyWaiting( "forking", "Forking in progress on GitHub, please wait" );
                        $timeout( $scope.annotateAfterForkCompletes, 10000 ); 
                        $scope.$apply();
                    }
                } );
            }
            
        } );

    };


    $scope.annotateAfterForkCompletes = function() {
        $scope.forkedRepo = gh.getRepo( $scope.username, "spa.coffeete.ch" ); 
        $scope.forkedRepo.read( "gh-pages", "cities.json", function(err, data) { 
            if( err ) {
                $timeout( $scope.annotateAfterForkCompletes, 10000 );
            }
            else {
                $scope.notifyWaiting( "annotating", "Annotating data on GitHub" );
                // Write the new data into our repository
                $scope.appendQuirkToShop();

                function stripHashKey( key, value ) { if( key == "$$hashKey" ) { return undefined; } return value; } // <4>
                var newData = JSON.stringify( $scope.shops, stripHashKey, 2 );
                $scope.forkedRepo.write('gh-pages', $scope.city.name + '.json', newData, 'Added my quirky information', function(err) { // <5>
                    if( !err ) {
                        // Annotate our data using a pull request
                        var pull = {
                            title: "Adding quirky information to " + $scope.shopToAnnotate.name,
                            body: "Created by :" + $scope.username,
                            base: "gh-pages",
                            head: $scope.username + ":" + "gh-pages"
                        };
                        target = gh.getRepo( "xrd", "spa.coffeete.ch" ); // <6>
                        target.createPullRequest( pull, function( err, pullRequest ) {
                            if( !err ) { // <7>
                                $scope.notifyWaiting( "annotated", "Successfully sent annotation request" );
                                $timeout( function() { $scope.notifyWaiting( undefined ) }, 5000 );
                                $scope.$apply();
                            }
                        } );
                    }
                    $scope.$apply();
                });
            }
            $scope.$apply();
        } );
        
        $scope.notifyWaiting( "annotated" );
    };    

    $scope.notifyWaiting = function( state, msg ) {
        if( state ) {
            $scope.waiting = {};
            $scope.waiting.state = state;
            $scope.waiting.msg = msg;
        }
        else {
            $scope.waiting = undefined;
        }
    }

    $scope.appendQuirkToShop = function() {
        if( undefined == $scope.shopToAnnotate.information ) {
            $scope.shopToAnnotate.information = [];
        }
        $scope.shopToAnnotate.information.push( $scope.annotation );
    };

    
} ] );

