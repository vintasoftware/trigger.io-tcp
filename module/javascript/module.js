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

  function removePadding(dataBase64) {
    var x = dataBase64;
    for (var i = x.length - 1; i > 0 && x[i] == '='; i--) {}
    return dataBase64.slice(0, i + 1);
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
    this.maxBase64Length = config.maxBase64Length || 65536;
    // make sure maxBase64Length % 4 == 0 (base64 strings should be chunked 4 bytes at a time)
    this.maxBase64Length = this.maxBase64Length - (this.maxBase64Length % 4);
    this.buffer = [];
    this.totalBufferLength = 0;
    this.pipeline = new Pipeline();
    this.onError = config.onError || function () {};
    this.onClose = config.onClose || function () {};
  };

  Socket.prototype.connect = function (success, error) {
    forge.internal.call('tcp.createSocket', {ip: this.ip, port: this.port}, success, error);
  };

  Socket.prototype.sendByteArrayNow = function (dataBase64, success, error) {
    forge.internal.call('tcp.sendByteArray', {ip: this.ip, port: this.port, dataBase64: dataBase64}, success, error);
  };

  Socket.prototype.sendBufferFn = function (callback) {
    var _this = this;
    var error = this.onError;

    var data = this.buffer.shift(0);
    this.totalBufferLength -= data.length;

    this.sendByteArrayNow(data, sendMoreIfNeeded, this.onError);

    function sendMoreIfNeeded () {
      if (_this.buffer.length > 0) {
        _this.sendBufferFn(callback);
      } else {
        callback();
      }
    }
  };

  Socket.prototype.sendByteArray = function (dataBase64) {
    var pipeline = this.pipeline;

    if (dataBase64.length > this.maxBase64Length) {
      // split dataBase64 into chunks and add to buffer
      var dataBase64NoPad = removePadding(dataBase64);
      var splitData = split(dataBase64NoPad, this.maxBase64Length);
      extend(this.buffer, splitData);
    } else {
      this.buffer.push(dataBase64);
    }
    this.totalBufferLength += dataBase64.length;

    if (this.totalBufferLength >= this.maxBase64Length) {
      if (!this.pipeline.hasNext()) {
        // if there is not a next consumer in pipeline, add a new one
        pipeline.queue(this, this.sendBufferFn);
      }
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
