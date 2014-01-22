package io.trigger.forge.android.modules.tcp.model;

public class IPAndPort {
	
	private String ip;
	private int port;
	
	public IPAndPort(String ip, int port) {
		if (ip == null) {
			throw new IllegalArgumentException("Cannot create IPAndPort with null IP");
		}
		
		this.ip = ip;
		this.port = port;
	}
	
	public String getIP() {
		return ip;
	}
	
	public int getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IPAndPort)) {
			return false;
		}
		IPAndPort other = (IPAndPort) obj;
		if (ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!ip.equals(other.ip)) {
			return false;
		}
		if (port != other.port) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "IPAndPort[ip=" + ip + ", port=" + port + "]";
	}
	
}
