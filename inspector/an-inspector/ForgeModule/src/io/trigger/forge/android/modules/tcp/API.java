package io.trigger.forge.android.modules.tcp;

import io.trigger.forge.android.core.ForgeParam;
import io.trigger.forge.android.core.ForgeTask;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

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
			@ForgeParam("port") final int port) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try {
					facade.createSocket(ip, port);
					task.success();
				} catch (UnknownHostException e) {
					task.error(jsonException("an unknown ip/host was entered", e.getMessage(), "BAD_INPUT", "BAD_IP"));
				} catch (IOException e) {
					task.error(jsonException("IO error while connecting", e.getMessage(), "UNEXPECTED_FAILURE", "IO_ERROR"));
				}
			}
		});
	}
	
	public static void sendData(final ForgeTask task,
			@ForgeParam("ip") final String ip,
			@ForgeParam("port") final int port,
			@ForgeParam("data") final String data,
			@ForgeParam("charset") final String charset) {
		task.performAsync(new Runnable() {
			@Override
			public void run() {
				try { 
					byte[] dataByteArray = data.getBytes(charset);
					facade.sendByteArray(ip, port, dataByteArray);
					task.success();
				} catch (UnsupportedEncodingException e) {
					task.error(jsonException(e.getMessage(), e.getMessage(), "BAD_INPUT", "BAD_CHARSET"));
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
