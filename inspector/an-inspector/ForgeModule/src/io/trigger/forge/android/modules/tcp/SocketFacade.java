package io.trigger.forge.android.modules.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketFacade {

	public static SocketFacade instance;
	
	public synchronized static SocketFacade getInstance() {
		if (instance == null) {
			instance = new SocketFacade();
		}
		return instance;
	}
	
	private HashMap<IPAndPort, SocketThread> socketMap;
	private LinkedBlockingQueue<String> dataQueue;
	
	public SocketFacade() {
		socketMap = new HashMap<IPAndPort, SocketThread>();
		dataQueue = new LinkedBlockingQueue<String>();
	}
	
	public synchronized void createSocket(String ip, Integer port, String charset) throws
			UnknownHostException, IllegalCharsetNameException, UnsupportedCharsetException,
			IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = new SocketThread(ip, port, charset);
		socketMap.put(ipAndPort, socketThread);
		socketThread.start();
	}
	
	public void sendData(String ip, Integer port, String data)
			throws UnsupportedEncodingException, IOException,
			IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null) {
			socketThread.send(data);
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public void flushSocket(String ip, Integer port)
			throws IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null) {
			socketThread.flush();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public String readData(String ip, Integer port)
			throws UnsupportedEncodingException, IOException,
			IllegalArgumentException, InterruptedException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		
		if (socketMap.containsKey(ipAndPort)) {
			String took = dataQueue.take();
			return took;
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public void addDataToBeRead(String data) {
		dataQueue.add(data);
	}
	
	public synchronized void closeSocket(String ip, Integer port)
			throws IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null) {
			socketThread.close();
			socketMap.remove(ipAndPort);
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}

}
