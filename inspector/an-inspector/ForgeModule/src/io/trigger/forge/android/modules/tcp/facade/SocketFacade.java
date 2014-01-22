package io.trigger.forge.android.modules.tcp.facade;

import io.trigger.forge.android.modules.tcp.exception.ClosedSocketException;
import io.trigger.forge.android.modules.tcp.model.IPAndPort;
import io.trigger.forge.android.modules.tcp.model.SocketThread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;

public class SocketFacade {

	public static SocketFacade instance;
	
	public synchronized static SocketFacade getInstance() {
		if (instance == null) {
			instance = new SocketFacade();
		}
		return instance;
	}
	
	private HashMap<IPAndPort, SocketThread> socketMap;
	
	public SocketFacade() {
		socketMap = new HashMap<IPAndPort, SocketThread>();
	}
	
	public synchronized void createSocket(String ip, int port, String charset, int timeout) throws
			UnknownHostException, IllegalCharsetNameException,
			UnsupportedCharsetException, IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		
		if (!socketMap.containsKey(ipAndPort)) {
			SocketThread socketThread = new SocketThread(ip, port, charset, timeout);
			socketMap.put(ipAndPort, socketThread);
			socketThread.start();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public void sendData(String ip, int port, String data)
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
	
	public void flushSocket(String ip, int port)
			throws IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null) {
			socketThread.flush();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public String readData(String ip, int port)
			throws InterruptedException, UnsupportedEncodingException,
			IOException, ClosedSocketException,
			IllegalArgumentException, Exception {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketThread socketThread = socketMap.get(ipAndPort);
		
		if (socketThread != null) {
			return socketThread.read();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public synchronized void closeSocket(String ip, int port)
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
