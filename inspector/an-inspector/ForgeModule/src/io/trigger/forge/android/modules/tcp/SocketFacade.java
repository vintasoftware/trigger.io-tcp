package io.trigger.forge.android.modules.tcp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class SocketFacade {

	public static SocketFacade instance;
	
	public static synchronized SocketFacade getInstance() {
		if (instance == null) {
			instance = new SocketFacade();
		}
		return instance;
	}
	
	private Map<IPAndPort, SocketThread> socketMap;
	
	public SocketFacade() {
		socketMap = new HashMap<IPAndPort, SocketThread>();
	}
	
	public synchronized void createSocket(String ip, Integer port) throws UnknownHostException, IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = new SocketThread(ip, port);
		socketThread.start();
		socketMap.put(ipAndPort, socketThread);
	}
	
	public synchronized void sendByteArray(String ip, Integer port, byte[] data) throws IllegalArgumentException, IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null && socketThread.isAlive()) {
			socketThread.sendByteArray(data);
		} else if (socketThread == null) {
			throw new IllegalArgumentException("there is no open socket to this ip and port");
		} else {
			throw new IOException("Connection to this ip and port is already closed: " + ipAndPort);
		}
	}
	
	public synchronized void flushSocket(String ip, Integer port) throws IllegalArgumentException, IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null && socketThread.isAlive()) {
			socketThread.flush();
		} else if (socketThread == null) {
			throw new IllegalArgumentException("there is no open socket to this ip and port");
		} else {
			throw new IOException("Connection to this ip and port is already closed: " + ipAndPort);
		}
	}
	
	public synchronized void closeSocket(String ip, Integer port) throws IllegalArgumentException, IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null && socketThread.isAlive()) {
			socketThread.close();
			socketMap.remove(ipAndPort);
		} else if (socketThread == null) {
			throw new IllegalArgumentException("there is no open socket to this ip and port");
		} else {
			throw new IOException("Connection to this ip and port is already closed: " + ipAndPort);
		}
	}

}
