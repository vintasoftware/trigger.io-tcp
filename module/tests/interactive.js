module("tcp");

asyncTest("Send alice.txt to an echo server and check echo data", 1, function() {
  var ip = prompt("Please insert the echo server IP " +
    "(if this is an android emulator, 10.0.2.2 is the host localhost IP)", "10.0.2.2");
  var port = parseInt(prompt("Please insert the echo server port", "7"), 10);
  if (ip == null || isNaN(port)) {
    ok(false, "Please insert a valid ip and port");
    start();
    return;
  }
  var file = forge.inspector.getFixture("tcp", "alice.txt");

  forge.tcp.test.base64(file, function (base64String) {
    var echoedData = [];
    var echoedDataTotal = 0;

    var socket = new forge.tcp.Socket(ip, port, {
      onError: function (error) {
        ok(false, "unexpected error: " + JSON.stringify(error));
        start();
      }
    });
    socket.connect();
    socket.send(base64String);
    socket.flush();

    var processData = function (data) {
      echoedData.push(data);
      echoedDataTotal += data.length;
      
      if (echoedDataTotal < base64String.length) {
        socket.read(processData);
      } else if (echoedDataTotal > base64String.length) {
        socket.close();
        ok(false, "received more data than was sent");
        start();
      } else {
        socket.close();
        var echo = echoedData.join('');
        strictEqual(echo, base64String, "received string was equal to sent string");
        start();
      }
    };

    socket.read(processData);
  });
});
