/* eslint no-alert: 0 */

'use strict';

//
// Here is how to define your module
// has dependent on mobile-angular-ui
//
var app = angular.module('MiniProfile', ['ngResource', 'ngRoute']);

//
// You can configure ngRoute as always, but to take advantage of SharedState location
// feature (i.e. close sidebar on backbutton) you should setup 'reloadOnSearch: false'
// in order to avoid unwanted routing.
//
app.config(function($routeProvider) {
  $routeProvider.when('/', {templateUrl: 'profile.html', reloadOnSearch: false});
});

app.factory('Profile', ['$resource', function($resource) {
  // 「get」「find」「create」の3つのアクションを定義
  return $resource(
    'https://api.9jialu.com/profile/:id',
    {id: '@id'},
    {
      get: {method: 'GET', isArray: true},
      find: {method: 'GET'},
      create: {method: 'POST'}
    }
  );
}]);

//
// For this trivial demo we have just a unique MainController
// for everything
//
app.controller('MiniController', ['$rootScope', '$scope', 'Profile',
  function($rootScope, $scope, Profile) {
	alert("what");
	Profile.get({id:1}).$promise.then(function(user) {
		$scope.user = user;
	}).catch(function(data, status) {
	    alert('error');
  	});
}]);