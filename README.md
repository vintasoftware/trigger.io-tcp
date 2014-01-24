# trigger.io-tcp

## Overview
trigger.io module for TCP sockets (android-only for now)

## Usage
**tcp** module has an object-oriented interface for creating TCP Sockets. You can create a socket with `var socket = new forge.tcp.Socket(ip, port)`, connect it with `socket.connect()` and send data with `socket.send('hello world')`. You don't need to pass a callback on each method call, since calls will be ordered in a async-pipeline fashion. For optimization purposes, there is a JS side buffer. When you call `flush` or `close`, data will be sent from JS to native and then to network. To read data from a socket is necessary to pass a callback to get received data, like `socket.read(callback)`

### Example
```javascript
var socket = new forge.tcp.Socket('10.0.2.2', 7070);
socket.connect();
socket.send('hello world');
socket.flush();
socket.read(function (data) {
    console.log('received: ' + data);
    socket.close();
});
```

## Object-oriented API
### creating a socket
Use the `forge.tcp.Socket(ip, port, config)` constructor. `ip` must be a string and `port` an integer. `config` is optional. Don't forget the `new` keyword. Also, don't forget to `connect` it afterwards:
```javascript
var socket = new forge.tcp.Socket('10.0.2.2', 7);
```
To customize the socket, use the `config` argument. `config` is a object that supports the following properties and defaults:  

* `config.charset = 'UTF-8'` Java [Charset](http://docs.oracle.com/javase/6/docs/api/java/nio/charset/Charset.html) in which data will be encoded/decoded when the socket send/read data. This has nothing to do with JS or Java internal string encoding. This charset is needed because data will be sent/read as bytes in the native socket
* `config.connectionTimeout = 30000` connection timeout in ms
* `config.maxBufferLength = 65536` JS buffer length in JS chars. Data is buffered in JS side to avoid unnecessary repeated calls to the resource-intensive native bridge. Use `flush` method when you need to send data to native side without filling the buffer

The `config` also support the following socket events:
* `config.onConnect` function to be called immediately after the socket is connected
* `config.onClose` function to be called immediately after the socket is closed
* `config.onError` function to be called immediately after an error occurs. When an error occurs, all subsequent calls to socket methods will not be executed, i.e., an error interrupts a socket method pipeline

### socket methods
When you call a socket method, this call will be queued to the socket pipeline. This means you can call `send`, `flush`, `close` without using callbacks. The call will return immediately, but will be executed when the previous operation is completed, meaning that call order is preserved. Only `read` method needs a callback to get received data. See the [example in Usage](#example) section above

#### connecting
Use the `socket.connect()` to connect a socket to the server

#### sending data
Use the `socket.send(data)` method. `data` must be a string and will be encoded as `config.charset` bytes before is sent to network:
```javascript
var data = 'hello world';
socket.send(data);
```

#### flushing data
Data is buffered both in JS and native sides, so when you are developing for a send/read protocol you may need to flush data beforing reading:
```javascript
socket.flush();
```

#### reading data
Use the `socket.read(function (data) { ... })` method. `data` is the received data as string, decoded from bytes with `config.charset`:
```javascript
socket.read(function (data) {
    // do something with data...
    // call more socket methods...
});
```

#### closing the socket
Use the `socket.close()` method when you are done with it