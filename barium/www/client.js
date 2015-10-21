


var bariumClient = {
		//ws://127.0.0.1:8080/websocket
    connectionUrl : undefined,
    socket: undefined,
    //Used to connect
	connect : function(url){
	    bariumClient.connectionUrl = url;
        
	    var connected = false;
	    if(bariumClient.socket !== undefined){
	    	if(bariumClient.socket.readyState == WebSocket.OPEN){
	    		connected=true;
	    	}
	    }
	    
	    if (!window.WebSocket) {
	    	window.WebSocket = window.MozWebSocket;
	    }
	    
	    if (window.WebSocket && connected === false) {
	    	bariumClient.socket = new WebSocket(bariumClient.connectionUrl);
	    	
	    	bariumClient.socket.onmessage = function(event) {
	    		var ta = document.getElementById('responseText');
	    		ta.value = ta.value + '\n' + event.data
	    	};
	    	
	    	bariumClient.socket.onopen = function(event) {
	    		var ta = document.getElementById('responseText');
	    		ta.value = "Web Socket opened!";
	    	};
	    	
	    	bariumClient.socket.onclose = function(event) {
	    		var ta = document.getElementById('responseText');
	    		ta.value = ta.value + "Web Socket closed";
	    	};
	    	return true
	    } else {
	    	return false
	    	//alert("Your browser does not support Web Socket.");
	    }
	},
	send: function(message) {
		if (!window.WebSocket) {
			return;
		}
		if (bariumClient.socket.readyState == WebSocket.OPEN) {
			bariumClient.socket.send(JSON.stringify({"_eventType":"transform","message":message}));
		} else {
			alert("The socket is not open.");
		}
	}
   
};


bariumClient.connect("ws://127.0.0.1:8080/websocket")