// Create an application module for our demo.
        angular.module( "website", [ "ngAnimate" ] );
        // --------------------------------------------------------------------------- //
        // --------------------------------------------------------------------------- //
        // I control the root of the application.
        angular.module( "website" ).controller(
            "AppController",
            function AppController( $scope , $timeout) {
                
                // I determine which view is being rendered.
                $scope.view = "search";
                // I determine the orientation we will use when transitioning from one
                // view to the next.
                $scope.orientation = null;
                // ---
                // PUBLIC METHODS.
                // ---
                // I show the enemies list.
                $scope.showDetails = function( orientation ) {
					//alert('show Details method');
                    $scope.view = "details";
                    $scope.orientation = ( orientation || "forward" );
                };
                // I show the friends list.
                $scope.showSearches = function( orientation ) {
                    $scope.view = "search";
                    $scope.orientation = ( orientation || "forward" );
                };
				
				$scope.showMessage = true;
				
				$scope.save = function() {

				$scope.showMessage = false;
				//alert($scope.showMessage);

				// Simulate 2 seconds loading delay
				$timeout(function() {

					// Loadind done here - Show message for 3 more seconds.
					$timeout(function() {
						$scope.showMessage = true;
					}, 500);

				}, 1000);
				};
				
            }
        );
