module("tcp");

asyncTest("Send alice.txt to an echo server and check echo data", 2, function() {
  var file = forge.inspector.getFixture("tcp", "alice.txt");
  var port = 49200;

  forge.tcp.test.base64(file, function (base64String) {
    forge.logging.info('will start echo server...');

    forge.tcp.test.startEchoServer(port, function () {
      forge.logging.info('echo server running.');

      var echoedData = [];
      var echoedDataTotal = 0;

      var socket = new forge.tcp.Socket('127.0.0.1', port, {
        onError: function (error) {
          ok(false, "unexpected error: " + JSON.stringify(error));
          gracefullyExitTest();
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

          gracefullyExitTest();
        } else {
          socket.close();
          var echo = echoedData.join('');
          strictEqual(echo, base64String, "received string was equal to sent string");
          
          gracefullyExitTest();
        }
      };

      socket.read(processData);

      function gracefullyExitTest () {
        forge.tcp.test.stopEchoServer(function () {
          ok(true, "gracefully stopped echo server");
          start();
        }, function stopEchoServerError () {
          ok(false, "failed to stop echo server");
          start();
        });
      }
    }, function startEchoServerError () {
      ok(false, "failed to start echo server");
      start();
    });
  }, function base64Error() {
    ok(false, "failed to convert file to base64 string");
    start();
  });
});