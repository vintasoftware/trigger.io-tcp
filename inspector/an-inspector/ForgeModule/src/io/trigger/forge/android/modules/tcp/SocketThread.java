package io.trigger.forge.android.modules.tcp;

import io.trigger.forge.android.core.ForgeApp;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import com.google.gson.JsonObject;

public class SocketThread extends Thread {

	private String ip;
	private Integer port;
	private Socket socket;
	private BufferedReader in;
	private BufferedOutputStream out;
	
	public SocketThread(String ip, Integer port) throws UnknownHostException, IOException {
		this.ip = ip;
		this.port = port;
		socket = new Socket(ip, port);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new BufferedOutputStream(socket.getOutputStream());
	}
	
	public synchronized void sendByteArray(byte[] data) throws IOException {
		out.write(data);
	}
	
	public synchronized void flush() throws IOException {
		out.flush();
	}
	
	public synchronized void close() throws IOException {
		in.close();
		out.close();
		socket.close();
	}
	
	@Override
	public void run() {
		for (;;) {
			int received;
			try {
				received = in.read();
				
				if (received != -1) {
					JsonObject dataJson = new JsonObject();
					dataJson.addProperty("ip", ip);
					dataJson.addProperty("port", port);
					dataJson.addProperty("data", received);
					ForgeApp.event("tcp.onData", dataJson);
				} else {
					break;
				}
			} catch (IOException e) {
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("ip", ip);
				dataJson.addProperty("port", port);
				ForgeApp.event("tcp.onReadError", dataJson);
				break;
			}
		}
		
		try {
			this.close();
		} catch (IOException e) {
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("ip", ip);
			dataJson.addProperty("port", port);
			ForgeApp.event("tcp.onCloseError", dataJson);
		}
	}

}
