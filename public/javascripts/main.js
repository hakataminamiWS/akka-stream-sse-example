var evtSource = new EventSource('/sse');
var sseMessage = document.querySelector('#sse-message');

evtSource.addEventListener('message', function (e) {
    sseMessage.textContent = e.data + ";" + e.event + ";" + e.source;
});

let btn = document.querySelector('#get');

btn.addEventListener('click', function () {
    fetch('/get');
});