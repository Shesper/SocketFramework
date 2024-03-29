package me.ministrie.net.impl;

public interface NioService{
	public void startService();
	public String getIP();
	public int getPort();
	public NioService setIP(String ip);
	public NioService setPort(int port);
	public void close(boolean restart);
	public <T> T getServer(Class<? extends ServerCase> adapter);
	public IClient getClient();
}
