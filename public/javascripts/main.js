let evtSource = new EventSource('/sse');
let sseMessage = document.querySelector('#sse-message');

evtSource.addEventListener('signal', function (e) {
    sseMessage.textContent = e.data;
});

let btn = document.querySelector('#get');
btn.addEventListener('click', function () {
    fetch('/signal');
});