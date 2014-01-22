package io.trigger.forge.android.modules.tcp.model;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.modules.tcp.exception.ClosedSocketException;
import io.trigger.forge.android.modules.tcp.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.JsonObject;

public class SocketThread extends Thread {

	private String ip;
	private Integer port;
	private String charset;
	private int readBufferSize;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private LinkedBlockingQueue<DataMessage> dataQueue;

	public SocketThread(String ip, Integer port, String charset) throws
			UnknownHostException, IllegalCharsetNameException, UnsupportedCharsetException,
			IOException {
		this.ip = ip;
		this.port = port;
		this.readBufferSize = 8192;
		this.charset = Charset.forName(charset).name();
		this.socket = new Socket(ip, port);
		this.in = this.socket.getInputStream();
		this.out = this.socket.getOutputStream();
		this.dataQueue = new LinkedBlockingQueue<DataMessage>();
	}

	public String getIP() {
		return ip;
	}

	public Integer getPort() {
		return port;
	}

	public void send(String data)
			throws UnsupportedEncodingException, IOException {
		byte[] dataByteArray = data.getBytes(charset);
		out.write(dataByteArray);
	}

	public void flush() throws IOException {
		out.flush();
	}

	public void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}
	
	public String read()
			throws InterruptedException, UnsupportedEncodingException, IOException,
			ClosedSocketException, Exception {
		if (this.isAlive() || !dataQueue.isEmpty()) {
			DataMessage message = dataQueue.take();
			
			if (message.isData()) {
				return message.getData();
			} else {
				// may be: UnsupportedEncodingException, IOException
				//         ClosedSocketException or Exception
				throw message.getException();
			}
		} else {
			throw new ClosedSocketException();
		}
	}

	@Override
	public void run() {
		try {
			for (;;) {
				byte[] received = new byte[this.readBufferSize];
	
				int receivedCount = this.in.read(received);
	
				if (receivedCount != -1) {
					String data = Util.byteArrayToString(received, receivedCount, this.charset);
					this.dataQueue.add(new DataMessage(data));
				} else {
					JsonObject dataJson = new JsonObject();
					dataJson.addProperty("ip", ip);
					dataJson.addProperty("port", port);
					ForgeApp.event("tcp.onReadError", dataJson);
					
					try {
						// must close the socket to cancel pending sends
						this.close();
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
					
					throw new ClosedSocketException();
				}
			}
		} catch (Exception e) {
			this.dataQueue.add(new DataMessage(e));
		}
	}
}
