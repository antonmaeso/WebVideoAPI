var stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#headposition").html("");
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/headposition', function (headposition) {
            showHeadPosition(headposition.body);
        });
    });
}



function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}
var elem = null;
var positionY= null;
var positionX = null;
function startCamera() {
	elem = document.getElementById("animate");
	positionY = elem.getBoundingClientRect().y;
	positionX = elem.getBoundingClientRect().x;	
	let d;
    fetch("http://localhost:8080/startCamera").then(data=> d= data.json());
console.log(d);
    console.log("Camera started");
}



function showHeadPosition(message) {	    
	var distanceFromCenter = JSON.parse(message);
	var positionChangeRelativeY = positionY + (distanceFromCenter.y*0.1);
	var positionChangeRelativeX = positionX + (distanceFromCenter.x*0.1);
		
	elem.style.top = positionChangeRelativeY + "px"; 
	elem.style.left = positionChangeRelativeX + "px"; 
	
	$("#headposition").html("<tr><td>" + elem.style.top + "y--" + elem.style.left + "x" + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
	$( "#startCamera" ).click(function() { startCamera(); });
});


