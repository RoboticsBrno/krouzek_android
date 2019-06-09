'use strict';

var chat = document.getElementById("chat");
function addMessage(date, sender, message) {
    if(date !== null && date !== "") {
        var elDate = document.createElement("small");
        elDate.style.color = "gray";
        elDate.innerText = "[" + date + "] ";
        chat.appendChild(elDate)
    }

    if(sender !== null) {
        var elSender = document.createElement("b");
        elSender.innerHTML = "&lt;" + sender + "&gt;: ";
        chat.appendChild(elSender);
    }

    var isImg = message.match(new RegExp('^IMG: http[^ ]+$'));
    if(sender === null || !isImg) {
        var elMessage = document.createElement(sender !== null ? "span" : "i");
        elMessage.innerHTML = message;

        chat.appendChild(elMessage);
    } else {
        chat.appendChild(document.createElement("br"));

        var elImg = document.createElement("img");
        elImg.src = message.substring(5);
        elImg.style.marginLeft = "20px";
        elImg.style.border = "1px solid black";
        elImg.style.width = "70%";
        chat.appendChild(elImg);
    }
    chat.appendChild(document.createElement("br"));
    window.scrollTo(0, document.body.scrollHeight);
}
