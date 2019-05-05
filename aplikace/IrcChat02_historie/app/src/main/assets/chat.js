'use strict';

var chat = document.getElementById("chat");
function addMessage(sender, message) {
    if(sender !== null) {
        var elSender = document.createElement("b");
        elSender.innerHTML = "&lt;" + sender + "&gt;: ";
        chat.appendChild(elSender);
    }

    var elMessage = document.createElement(sender !== null ? "span" : "i");
    elMessage.innerHTML = message;

    chat.appendChild(elMessage);
    chat.appendChild(document.createElement("br"));
    window.scrollTo(0, document.body.scrollHeight);
}
