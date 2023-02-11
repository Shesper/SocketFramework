package me.ministrie.utils;

import java.net.InetSocketAddress;
import java.util.Map.Entry;
import java.util.Optional;

import io.netty.channel.Channel;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class VisualServer{
	private String id;
	private String ip;
	private int port;
	private String servername;
	private ServerInfo server;
	
	public VisualServer(String id, String ip, int port){
		this.id = id;
		this.ip = ip;
		this.port = port;
		Optional<Entry<String, ServerInfo>> opt = BungeeCord.getInstance().getServers().entrySet().stream().filter(serv -> {
			InetSocketAddress address = (InetSocketAddress) serv.getValue().getSocketAddress();
			return address.getAddress().getHostAddress().equals(ip) && address.getPort() == port;
		}).findFirst();
		if(opt.isPresent()){
			this.server = opt.get().getValue();
			this.servername = server.getName();
		}
	}
	
	public String getServerName(){
		return servername;
	}
	
	public ServerInfo getServer(){
		return server;
	}
	
	public String getID(){
		return id;
	}
	
	public String getIP(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	@Override
	public String toString(){
		return "visualserver:" + port;
	}
	
	public static VisualServer parsing(Channel ctx, String message){
		String[] sp = message.split(":");
		if(sp.length == 2){
			if(sp[0].equals("visualserver")){
				try{
					int port = Integer.parseInt(sp[1]);
					InetSocketAddress address = (InetSocketAddress) ctx.remoteAddress();
					return new VisualServer(ctx.id().asShortText(), address.getAddress().getHostAddress(), port);
				}catch(Exception e){
					return null;
				}
			}
		}
		return null;
	}
}
