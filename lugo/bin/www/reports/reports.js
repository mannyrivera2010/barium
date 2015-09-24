var reports = {
		getFileList:function(){
		    $.get( "list", function( data ) {
		    	  $( "#list" ).html( data );
	    	});
		},
		getAvailableReports:function(){
		    $.get( "availableReports", function( data ) {
		    	  $( "#availableReports" ).html( data );
	    	});
		},
		getAvailableProjects:function(){
		    $.ajax({
		        url: 'availableProjects',
		        type: 'GET',
		        success: function(data){ 
		        	$( "#availableProjects" ).html( data );
		        },
		        error: function(data) {
		            alert('woops!'); //or whatever
		        }
		    });
		},
		getTagsForProject:function(){
		    $.ajax({
		        url: "tagsForProjects/" + $("#project").val(),
		        type: 'GET',
		        success: function(data){ 
		        	$( "#tagsForProjects" ).html( data );
		        },
		        error: function(data) {
		            alert('Error!');
		        }
		    });
		},
		runReport:function(){
			$("#runButton").attr("disabled","disabled");
			
		    $.ajax({
		        url: "run/" + $("#report").val() + "/" + $("#project").val() + "/" + $("#tagid").val(),
		        type: 'GET',
		        success: function(data){ 
		        	$( "#reports" ).html( data );
		        },
		        error: function(data) {
		            alert('Error!');
		            $("#runButton").removeAttr("disabled");
		        }
		    });
		}	
}