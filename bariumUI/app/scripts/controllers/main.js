'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description # MainCtrl Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller(
		'MainCtrl',
		function($scope, $position, $http) {

			if ($scope.taskHistoryList === undefined) {
				$http.get("http://localhost:8080/queue.json").success(
						function(response) {
							$scope.taskHistoryList = response.tasksStatus;
							// console.log(response);
						}).error(function(data, status, headers, config) {
					// log error
							$scope.taskHistoryList = [];
							console.log(headers);
							
				});
			}

		});
