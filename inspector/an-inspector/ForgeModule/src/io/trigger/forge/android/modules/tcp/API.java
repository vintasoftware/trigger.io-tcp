package io.trigger.forge.android.modules.tcp;

import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import com.google.gson.JsonObject;

public class API {
	
	private static SocketFacade facade = SocketFacade.getInstance();
	
	private static JsonObject jsonException(String message, String originalMessage, String type, String subtype) {
		// see error callback standards: https://trigger.io/docs/current/getting_started/api.html
		JsonObject json = new JsonObject();
		json.addProperty("message", message);
		json.addProperty("type", type);
		json.addProperty("subtype", subtype);
		
		json.addProperty("originalMessage", originalMessage);
		return json;
	}
	
	public static void createSocket(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port,
			@ForgeParam("charset") final String charset) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try {
					facade.createSocket(ip, port, charset);
					task.success();
				} catch (UnknownHostException e) {
					task.error(jsonException("An unknown ip/host was entered", e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IllegalCharsetNameException e) {
					task.error(jsonException("An illegal charset name was entered", e.getMessage(), "BAD_INPUT", "BAD_CHARSET"));
				} catch (UnsupportedCharsetException e) {
					task.error(jsonException("An unsupported charset name was entered", e.getMessage(), "BAD_INPUT", "BAD_CHARSET"));
				} catch (IOException e) {
					task.error(jsonException("IO error while connecting", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				}
			}
		});
	}
	
	public static void sendData(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port,
			@ForgeParam("data") final String data) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try { 
					facade.sendData(ip, port, data);
					task.success();
				} catch (UnsupportedEncodingException e) {
					task.error(jsonException("An unsupported encoding for this socket charset", e.getMessage(), "BAD_INPUT", "BAD_CHARSET"));
				} catch (IllegalArgumentException e) {
					task.error(jsonException(e.getMessage(), e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IOException e) {
					task.error(jsonException("IO error while sending data", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				} 
			}
		});
	}
	
	public static void flushSocket(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try {
					facade.flushSocket(ip, port);
					task.success();
				} catch (IllegalArgumentException e) {
					task.error(jsonException(e.getMessage(), e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IOException e) {
					task.error(jsonException("IO error while sending data", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				}
			}
		});
	}
	
	public static void readData(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try { 
					String data = facade.readData(ip, port);
					task.success(data);
				} catch (UnsupportedEncodingException e) {
					task.error(jsonException("An unsupported encoding for this socket charset", e.getMessage(), "BAD_INPUT", "BAD_CHARSET"));
				} catch (IllegalArgumentException e) {
					task.error(jsonException(e.getMessage(), e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IOException e) {
					task.error(jsonException("IO error while reading data", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				} catch (InterruptedException e) {
					task.error(jsonException("Interrupted thread while reading data", e.getMessage(), "UNEXPECTED_FAILURE", "THREAD_ERROR"));
				} 
			}
		});
	}
	
	public static void closeSocket(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try {
					facade.closeSocket(ip, port);
					task.success();
				} catch (IllegalArgumentException e) {
					task.error(jsonException(e.getMessage(), e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IOException e) {
					task.error(jsonException("IO error while sending data", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				}
			}
		});
	}
}
