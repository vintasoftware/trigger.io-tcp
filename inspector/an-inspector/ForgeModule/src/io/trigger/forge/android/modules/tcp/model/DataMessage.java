package io.trigger.forge.android.modules.tcp.model;

public class DataMessage {
	
	private String data;
	private Exception exception;
	
	public DataMessage(String data) {
		this.data = data;
		this.exception = null;
	}
	
	public DataMessage(Exception exception) {
		this.data = null;
		this.exception = exception;
	}
	
	public boolean isData() {
		return this.data != null;
	}
	
	public String getData() {
		return data;
	}
	
	public Exception getException() {
		return exception;
	}
}
