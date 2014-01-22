package io.trigger.forge.android.modules.tcp.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.util.Log;

public class EchoServer extends Thread {
	
	private static EchoServer instance;
	
	public static EchoServer startThread(int port) {
		if (instance == null) {
			instance = new EchoServer(port);
			instance.start();
			return instance;
		} else {
			throw new IllegalArgumentException("EchoServer already running. " +
					"Please use stopThread.");
		}
	}
	
	public static boolean checkAndStopThread() {
		if (instance != null) {
			if (instance.isAlive()) {
				instance.interrupt();
				instance = null;
				
				// returns false since server ended forcefully
				return false;
			} else {
				// returns true since server ended gracefully
				return true;
			}
		} else {
			throw new IllegalArgumentException("There wasn't an EchoServer running. " +
					"Please use startThread before this method.");
		}
	}
	
	private static final String TAG = "EchoServer";
	private int port;
	private int readBufferSize;
	
	private EchoServer(int port) {
		this.port = port;
		this.readBufferSize = 65536;
	}
	
	@Override
	public void run() {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		OutputStream out = null;
		InputStream in = null;
		
		try {
        	serverSocket = new ServerSocket(this.port);
            clientSocket = serverSocket.accept();     
            out = clientSocket.getOutputStream();
            in = clientSocket.getInputStream();
            
            byte[] received = new byte[this.readBufferSize];
            int receivedCount = 0;
            
        	while ((receivedCount = in.read(received)) != -1) {
                out.write(received, 0, receivedCount);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
        	try {
        		if (in != null) in.close();
            	if (out != null) out.close();
            	if (clientSocket != null) clientSocket.close();
            	if (serverSocket != null) serverSocket.close();
        	} catch (Exception e) {
        		Log.e(TAG, e.getMessage(), e);
        	}
        }
	}
}
