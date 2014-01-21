package io.trigger.forge.android.modules.tcp;

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

public class SocketThread extends Thread {

	private String ip;
	private Integer port;
	private Charset charset;
	private int readBufferSize;
	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private volatile boolean isOpen;
	private LinkedBlockingQueue<DataMessage> dataQueue;

	public SocketThread(String ip, Integer port, String charset) throws
			UnknownHostException, IllegalCharsetNameException, UnsupportedCharsetException,
			IOException {
		this.ip = ip;
		this.port = port;
		this.readBufferSize = 8192;
		this.charset = Charset.forName(charset);
		this.socket = new Socket(ip, port);
		this.in = this.socket.getInputStream();
		this.out = this.socket.getOutputStream();
		this.isOpen = true;
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
		byte[] dataByteArray = data.getBytes(charset.name());
		out.write(dataByteArray);
	}

	public void flush() throws IOException {
		out.flush();
	}

	public void close() throws IOException {
		isOpen = false;
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

	private String byteArrayToString(byte[] byteArray, int length)
			throws UnsupportedEncodingException {
		byte[] receivedCorrect = new byte[length];

		for (int i = 0; i < length; i++) {
			receivedCorrect[i] = byteArray[i];
		}

		return new String(receivedCorrect, charset.name());
	}

	@Override
	public void run() {
		try {
			while (this.isOpen) {
				byte[] received = new byte[this.readBufferSize];
	
				int receivedCount = this.in.read(received);
	
				if (receivedCount != -1) {
					String data = byteArrayToString(received, receivedCount);
					this.dataQueue.add(new DataMessage(data));
				} else {
					throw new ClosedSocketException();
				}
			}
		} catch (Exception e) {
			this.dataQueue.add(new DataMessage(e));
		}
	}
}
