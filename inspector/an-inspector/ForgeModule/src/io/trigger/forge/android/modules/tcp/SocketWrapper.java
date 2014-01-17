package io.trigger.forge.android.modules.tcp;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

public class SocketWrapper {

	private String ip;
	private Integer port;
	private Charset charset;
	private int readBufferSize;
	private Socket socket;
	private InputStream in;
	private BufferedOutputStream out;
	
	public SocketWrapper(String ip, Integer port, String charset) throws
			UnknownHostException, IllegalCharsetNameException, UnsupportedCharsetException,
			IOException {
		this.ip = ip;
		this.port = port;
		this.readBufferSize = 8192;
		this.charset = Charset.forName(charset);
		socket = new Socket(ip, port);
		in = socket.getInputStream();
		out = new BufferedOutputStream(socket.getOutputStream());
	}
	
	public String getIP() {
		return ip;
	}
	
	public Integer getPort() {
		return port;
	}
	
	public synchronized void send(String data)
			throws UnsupportedEncodingException, IOException {
		byte[] dataByteArray = data.getBytes(this.charset.name());
		out.write(dataByteArray);
	}
	
	public synchronized void flush() throws IOException {
		out.flush();
	}
	
	private String byteArrayToString(byte[] byteArray, int length)
			throws UnsupportedEncodingException {
		byte[] receivedCorrect = new byte[length];
		
		for (int i = 0; i < length; i++) {
			receivedCorrect[i] = byteArray[i];
		}
		
		return new String(receivedCorrect, this.charset.name());
	}
	
	public synchronized String read()
			throws UnsupportedEncodingException, IOException {
		byte[] received = new byte[this.readBufferSize];
		String result = null;
		
		int receivedCount = in.read(received);
		
		if (receivedCount != -1) {
			result = byteArrayToString(received, receivedCount);
		} else {
			this.close();
		}
		
		return result;
	}
	
	public synchronized void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}
}
