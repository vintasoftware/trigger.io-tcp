module("tcp");

asyncTest("Send alice.txt and check echo", 1, function() {
  var file = forge.inspector.getFixture("tcp", "alice.txt");

  forge.tcp.base64(file, function (base64String) {
    var echoedData = [];
    var echoedDataTotal = 0;

    var socket = new forge.tcp.Socket('10.0.2.2', 9100);
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
  }, function base64Error() {
    ok(false, "failed to convert file to base64 string");
    start();
  });
});