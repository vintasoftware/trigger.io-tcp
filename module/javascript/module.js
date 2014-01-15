// Expose the native API to javascript

forge.tcp = (function () {
  var exports = {};

  // Helper functions
  function split(str, len) {
    var chunks = [],
        i = 0,
        n = str.length;

    while (i < n) {
      chunks.push(str.slice(i, i += len));
    }

    return chunks;
  }

  function extend(arr, otherArr) {
      otherArr.forEach(function (v) { this.push(v); }, arr);
  }

  // Pipeline object
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

  // Socket object
  var Socket = function (ip, port, config) {
    config = config || {};

    this.ip = ip;
    this.port = port;
    this.charset = config.charset || 'UTF-8';
    this.maxBufferLength = config.maxBufferLength || 65536;
    this.buffer = [];
    this.totalBufferLength = 0;
    this.pipeline = new Pipeline();
    this.onConnect = config.onConnect || function (callback) { callback(); };
    this.onError = config.onError || function () {};
    this.onClose = config.onClose || function () {};
  };

  Socket.prototype.connectNow = function (success, error) {
    forge.internal.call('tcp.createSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.connectFn = function (callback) {
    var error = this.onError;

    this.connectNow(callback, error);
  };

  Socket.prototype.connect = function () {
    this.pipeline.queue(this, this.connectFn);
    this.pipeline.queue(this, this.onConnect);
  };

  Socket.prototype.sendNow = function (data, success, error) {
    forge.internal.call('tcp.sendData', {ip: this.ip, port: this.port, data: data, charset: this.charset}, success, error);
  };

  Socket.prototype.makeSenderFn = function (data) {
    var error = this.onError;
    
    return function (callback) {
      this.sendNow(data, callback, error);
    };
  };

  Socket.prototype.sendBufferHead = function () {
    var data = this.buffer.shift(0);
    this.totalBufferLength -= data.length;
    
    var sendFn = this.makeSenderFn(data);
    this.pipeline.queue(this, sendFn);
  };

  Socket.prototype.send = function (data) {
    if (data.length > this.maxBufferLength) {
      var dataChunks = split(data, this.maxBufferLength);
      extend(this.buffer, dataChunks);
    } else if (this.buffer.length > 0) {
      var last = this.buffer.pop();
      var lastAndNew = last.concat(data);
      var lastAndNewChunks = split(lastAndNew, this.maxBufferLength);
      extend(this.buffer, lastAndNewChunks);
    } else {
      this.buffer.push(data);
    }
    this.totalBufferLength += data.length;

    while (this.totalBufferLength > this.maxBufferLength) {
      this.sendBufferHead();
    }
  };

  Socket.prototype.flushNow = function (success, error) {
    forge.internal.call('tcp.flushSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.flushFn = function (callback) {
    var error = this.onError;
    
    this.flushNow(callback, error);
  };

  Socket.prototype.flush = function () {
    while (this.totalBufferLength > 0) {
      this.sendBufferHead();
    }

    this.pipeline.queue(this, this.flushFn);
  };

  Socket.prototype.closeNow = function (success, error) {
    forge.internal.call('tcp.closeSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.closeFn = function (callback) {
    var error = this.onError;

    this.closeNow(callback, error);
  };

  Socket.prototype.close = function () {
    this.flush();
    this.pipeline.queue(this, this.closeFn);
    this.pipeline.queue(this, this.onClose);
  };

  exports.Socket = Socket;

  // Events
  //forge.internal.addEventListener("tcp.onData", function (dataJson) {
  //    
  //});

  return exports;
}());
