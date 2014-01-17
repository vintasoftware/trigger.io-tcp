package io.trigger.forge.android.modules.tcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
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
	
	private Map<IPAndPort, SocketWrapper> socketMap;
	
	public SocketFacade() {
		socketMap = new HashMap<IPAndPort, SocketWrapper>();
	}
	
	public synchronized void createSocket(String ip, Integer port, String charset) throws
			UnknownHostException, IllegalCharsetNameException, UnsupportedCharsetException,
			IOException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketWrapper socketWrapper = new SocketWrapper(ip, port, charset);
		socketMap.put(ipAndPort, socketWrapper);
	}
	
	public synchronized void sendData(String ip, Integer port, String data)
			throws UnsupportedEncodingException, IOException,
			IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketWrapper socketWrapper = socketMap.get(ipAndPort);
		
		if (socketWrapper != null) {
			socketWrapper.send(data);
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public synchronized void flushSocket(String ip, Integer port)
			throws IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketWrapper socketWrapper = socketMap.get(ipAndPort);
		
		if (socketWrapper != null) {
			socketWrapper.flush();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public synchronized String readData(String ip, Integer port)
			throws UnsupportedEncodingException, IOException,
			IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketWrapper socketWrapper = socketMap.get(ipAndPort);
		
		if (socketWrapper != null) {
			return socketWrapper.read();
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}
	
	public synchronized void closeSocket(String ip, Integer port)
			throws IOException, IllegalArgumentException {
		IPAndPort ipAndPort = new IPAndPort(ip, port);
		SocketWrapper socketWrapper = socketMap.get(ipAndPort);
		
		if (socketWrapper != null) {
			socketWrapper.close();
			socketMap.remove(ipAndPort);
		} else {
			throw new IllegalArgumentException("There is no open socket to this ip and port: " + ipAndPort);
		}
	}

}
