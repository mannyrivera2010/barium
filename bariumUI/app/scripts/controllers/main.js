'use strict';
/**
 * @ngdoc function
 * @name sbAdminApp.controller:MainCtrl
 * @description # MainCtrl Controller of the sbAdminApp
 */
angular.module('sbAdminApp').controller(
		'MainCtrl',
		function($scope, $position, $http) {
//			var subscription = events.subscribe('/event/taskStatus', function(obj) {
//				var ta = document.getElementById('responseText');
//				ta.value = ta.value + '\n' + event.data
//			});
			
			$scope.progressString = "0% Complete (0 out of 0 scheduled completed)";
			$scope.percentInt = 0;
			
			var subscriptionProgress = events.subscribe('/event/progress', function(item) {
				//console.log(item.obj.updateString);
				$scope.progressString = item.obj.updateString;
				$scope.percentInt = item.obj.percentInt;
				
				$scope.$apply() 
			});
			
			var subscriptionTaskStatus = events.subscribe('/event/taskStatus', function(item) {
				console.log(item.obj);
			
			});
			
			
			if ($scope.taskHistoryList === undefined) {
				$http.get("http://localhost:8080/queue").success(
						function(response) {
							$scope.taskHistoryList = response.tasksStatus;
							$scope.progressString = response.progress.updateString;
							$scope.percentInt = response.progress.percentInt;
							
							// console.log(response);
						}).error(function(data, status, headers, config) {
					// log error
							$scope.taskHistoryList = [];
							console.log(headers);
							
				});
			}

		});



var events = (function(){
  var topics = {};
  var hOP = topics.hasOwnProperty;

  return {
    subscribe: function(topic, listener) {
      // Create the topic's object if not yet created
      if(!hOP.call(topics, topic)) topics[topic] = [];

      // Add the listener to queue
      var index = topics[topic].push(listener) -1;

      // Provide handle back for removal of topic
      return {
        remove: function() {
          delete topics[topic][index];
        }
      };
    },
    publish: function(topic, info) {
      // If the topic doesn't exist, or there's no listeners in queue, just leave
      if(!hOP.call(topics, topic)) return;

      // Cycle through topics queue, fire!
      topics[topic].forEach(function(item) {
      		item(info != undefined ? info : {});
      });
    }
  };
})();

var bariumClient = {
	// ws://127.0.0.1:8080/websocket
	connectionUrl : undefined,
	socket : undefined,
	// Used to connect
	connect : function(url) {
		bariumClient.connectionUrl = url;

		var connected = false;
		if (bariumClient.socket !== undefined) {
			if (bariumClient.socket.readyState == WebSocket.OPEN) {
				connected = true;
			}
		}

		if (!window.WebSocket) {
			window.WebSocket = window.MozWebSocket;
		}

		if (window.WebSocket && connected === false) {
			bariumClient.socket = new WebSocket(bariumClient.connectionUrl);

			bariumClient.socket.onmessage = function(event) {
				
				var eventJsonObj = JSON.parse(event.data)
				events.publish('/event/all', {'obj':eventJsonObj});
				
				//console.log(eventJsonObj);
				
				events.publish('/event/'+eventJsonObj._eventType, {'obj':eventJsonObj});
				
			};

			bariumClient.socket.onopen = function(event) {
				events.publish('/websocket/onopen', {
					'event': event
				});
				
				//var ta = document.getElementById('responseText');
				//ta.value = "Web Socket opened!";
			};

			bariumClient.socket.onclose = function(event) {
				events.publish('/websocket/onclose', {
					'event': event
				});
				//var ta = document.getElementById('responseText');
				//ta.value = ta.value + "Web Socket closed";
			};
			return true
		} else {
			return false
			// alert("Your browser does not support Web Socket.");
		}
	},
	send : function(message) {
		if (!window.WebSocket) {
			return;
		}
		if (bariumClient.socket.readyState == WebSocket.OPEN) {
			bariumClient.socket.send(JSON.stringify({
				"_eventType" : "transform",
				"message" : message
			}));
		} else {
			alert("The socket is not open.");
		}
	}

};

bariumClient.connect("ws://127.0.0.1:8080/websocket")
