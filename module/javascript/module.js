// Expose the native API to javascript

forge.tcp = (function () {
  var exports = {};

  // Helper functions
  function noop() {
  
  }

  function arrayfyArguments(args) {
    var argsArr = [];
    
    for (var i = 0; i < args.length; i++) {
        argsArr.push(args[i]);
    }

    return argsArr;
  }
    
  function convertToContinuation(fnContext, fn) {
    return function () {
      // callback should be the last arg,
      // other args should be given to original fn
      var args = arrayfyArguments(arguments);
      var callback = args.pop();

      fn.apply(fnContext, args);
      callback();
    };
  }

  var noopContinuation = convertToContinuation(null, noop);

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

  Pipeline.prototype.interrupt = function () {
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

      // call nextFn preserving given arguments
      var args = arrayfyArguments(arguments);
      nextFn.apply(null, args); // null preserves binding
    } else {
      this.running = false;
    }
  };

  Pipeline.prototype.callAndChain = function (fn, args) {
    var callNextFn = this.callNextFn.bind(this);
    
    this.running = true;

    // call fn with callNextFn as a callback,
    // without losing other arguments for fn
    args = arrayfyArguments(args);
    args.push(callNextFn);
    fn.apply(null, args); // null preserves binding
  };

  Pipeline.prototype.queue = function (fnContext, fn) {
    var _this = this;
    fn = fn.bind(fnContext);

    if (!this.isRunning()) {
      // if its not running, call now!
      this.callAndChain(fn, []);
    } else {
      // if its running, queue
      this.fnList.push(function () {
        _this.callAndChain(fn, arguments);
      });
    }
  };

  // Socket object
  var Socket = function (ip, port, config) {
    config = config || {};

    this.ip = ip;
    this.port = port;
    this.charset = config.charset || 'UTF-8';
    this.connectionTimeout = config.connectionTimeout || 30000;
    this.maxBufferLength = config.maxBufferLength || 65536;
    this.buffer = [];
    this.totalBufferLength = 0;
    this.pipeline = new Pipeline();
    
    var onConnect = config.onConnect || noop;
    var onClose = config.onClose || noop;
    this.onConnect = convertToContinuation(this, onConnect);
    this.onClose = convertToContinuation(this, onClose);

    this.customOnError = config.onError || noop;
  };

  Socket.prototype.onError = function (error) {
    this.pipeline.interrupt();
    this.customOnError(error);
  };

  Socket.prototype.getOnErrorFn = function () {
    return this.onError.bind(this);
  };

  Socket.prototype.connectNow = function (success, error) {
    forge.internal.call('tcp.createSocket', {ip: this.ip, port: this.port, charset: this.charset, timeout: this.connectionTimeout}, success, error);
  };

  Socket.prototype.connectFn = function (callback) {
    var error = this.getOnErrorFn();

    this.connectNow(callback, error);
  };

  Socket.prototype.connect = function () {
    this.pipeline.queue(this, this.connectFn);
    this.pipeline.queue(this, noopContinuation);
    this.pipeline.queue(this, this.onConnect);
  };

  Socket.prototype.sendNow = function (data, success, error) {
    forge.internal.call('tcp.sendData', {ip: this.ip, port: this.port, data: data}, success, error);
  };

  Socket.prototype.makeSenderFn = function (data) {
    var error = this.getOnErrorFn();
    
    return function (callback) {
      this.sendNow(data, callback, error);
    };
  };

  Socket.prototype.sendBufferHead = function () {
    var data = this.buffer.shift(0);
    this.totalBufferLength -= data.length;
    
    var sendFn = this.makeSenderFn(data);
    this.pipeline.queue(this, sendFn);
    this.pipeline.queue(this, noopContinuation);
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
    var error = this.getOnErrorFn();
    
    this.flushNow(callback, error);
  };

  Socket.prototype.flush = function () {
    while (this.totalBufferLength > 0) {
      this.sendBufferHead();
    }

    this.pipeline.queue(this, this.flushFn);
    this.pipeline.queue(this, noopContinuation);
  };

  Socket.prototype.readNow = function (success, error) {
    forge.internal.call('tcp.readData', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.readFn = function (callback) {
    var error = this.getOnErrorFn();
    
    this.readNow(callback, error);
  };

  Socket.prototype.read = function (readCallback) {
    var readCallbackCont = convertToContinuation(this, readCallback);

    this.pipeline.queue(this, this.readFn);
    this.pipeline.queue(this, readCallbackCont);
  };

  Socket.prototype.closeNow = function (success, error) {
    forge.internal.call('tcp.closeSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.closeFn = function (callback) {
    var error = this.getOnErrorFn();

    this.closeNow(callback, error);
  };

  Socket.prototype.close = function () {
    this.flush();
    this.pipeline.queue(this, this.closeFn);
    this.pipeline.queue(this, noopContinuation);
    this.pipeline.queue(this, this.onClose);
  };

  exports.Socket = Socket;

  // below are functions used for testing purposes
  exports.test = {};

  exports.test.base64 = function (file, success, error) {
    forge.internal.call("tcp.base64", file, success, error);
  };

  exports.test.startEchoServer = function (port, success, error) {
    forge.internal.call("tcp.startEchoServer", {port: port}, success, error);
  };

  exports.test.stopEchoServer = function (success, error) {
    forge.internal.call("tcp.stopEchoServer", {}, success, error);
  };

  return exports;
}());
