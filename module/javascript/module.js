// Expose the native API to javascript

forge.tcp = (function () {
  var exports = {};

  var Pipeline = function () {
    this.fnList = [];
    this.running = false;
  };

  Pipeline.prototype.hasNext = function () {
    return this.fnList.length !== 0;
  };

  Pipeline.prototype.isRunning = function () {
    return this.running;
  };

  Pipeline.prototype.callNextFn = function () {
    if (this.hasNext()) {
      var nextFn = this.fnList.shift();
      nextFn();
    } else {
      this.running = false;
    }
  };

  Pipeline.prototype.callAndChain = function (fn) {
    var callNextFn = this.callNextFn.bind(this);
    
    this.running = true;
    fn(callNextFn);
  };

  Pipeline.prototype.queue = function (fnContext, fn) {
    var _this = this;
    fn = fn.bind(fnContext);

    if (!this.isRunning()) {
      // if its not running, call now!
      this.callAndChain(fn);
    } else {
      // if its running, queue
      this.fnList.push(function () {
        _this.callAndChain(fn);
      });
    }
  };

  var Socket = function (ip, port, config) {
    config = config || {};

    this.ip = ip;
    this.port = port;
    this.maxBufferSize = config.maxBufferSize || 65536;
    this.maxNativeBridgeDataSize = config.maxNativeBridgeDataSize || 65536;
    this.buffer = [];
    this.pipeline = new Pipeline();
    this.onError = config.onError || function () {};
    this.onClose = config.onClose || function () {};
  };

  Socket.prototype.connect = function (success, error) {
    forge.internal.call('tcp.createSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.sendByteArrayNow = function (data, success, error) {
    forge.internal.call('tcp.sendByteArray', {ip: this.ip, port: this.port, data: data}, success, error);
  };

  Socket.prototype.sendBufferFn = function (callback) {
    var _this = this;
    var data = this.buffer.splice(0, this.maxNativeBridgeDataSize);

    this.sendByteArrayNow(data, sendMoreIfNeeded, this.onError);

    function sendMoreIfNeeded () {
      if (_this.buffer.length > 0) {
        _this.sendBufferFn(callback);
      } else {
        callback();
      }
    }
  };

  Socket.prototype.sendByte = function (b) {
    var pipeline = this.pipeline;

    this.buffer.push(b);

    if (this.buffer.length >= this.maxBufferSize) {
      if (!this.pipeline.hasNext()) {
        // if there is not a next consumer in pipeline, add a new one
        pipeline.queue(this, this.sendBufferFn);
      }
    }
  };

  Socket.prototype.sendByteArray = function (byteArray) {
    for (var i = 0; i < byteArray.length; i++) {
      this.sendByte(byteArray[i]);
    }
  };

  Socket.prototype.sendBase64String = function (base64String) {
    for (var i = 0; i < base64String.length; i++) {
        this.sendByte(base64String.charCodeAt(i));
    }
  };

  Socket.prototype.flushNow = function (success, error) {
    forge.internal.call('tcp.flushSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.flushFn = function (callback) {
    var _this = this;
    var error = this.onError;

    if (this.buffer.length > 0) {
      this.sendBufferFn(function () {
        _this.flushNow(callback, error);
      });
    } else {
      this.flushNow(callback, error);
    }
  };

  Socket.prototype.flush = function () {
    this.pipeline.queue(this, this.flushFn);
  };

  Socket.prototype.closeNow = function (success, error) {
    forge.internal.call('tcp.closeSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.closeFn = function (callback) {
    var _this = this;
    var error = this.onError;

    this.closeNow(closeCallback, error);

    function closeCallback () {
      callback();
      _this.onClose();
    }
  };

  Socket.prototype.close = function () {
    this.flush();
    this.pipeline.queue(this, this.closeFn);
  };

  exports.Socket = Socket;

  return exports;
}());
