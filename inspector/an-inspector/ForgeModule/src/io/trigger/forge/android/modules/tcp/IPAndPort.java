package io.trigger.forge.android.modules.tcp;

public class IPAndPort {
	
	private String ip;
	private Integer port;
	
	public IPAndPort(String ip, Integer port) {
		this.ip = ip;
		this.port = port;
	}
	
	public String getIP() {
		return ip;
	}
	
	public Integer getPort() {
		return port;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
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
		return ip.equals(other.ip) && port.equals(other.port);
	}

	@Override
	public String toString() {
		return "IPAndPort[ip=" + ip + ", port=" + port + "]";
	}
	
}
