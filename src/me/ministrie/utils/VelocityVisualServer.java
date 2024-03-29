package me.ministrie.utils;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.util.Optional;

import io.netty.channel.Channel;
import me.ministrie.velocity.VelocityMain;

public class VelocityVisualServer{
	private String id;
	private String ip;
	private int port;
	private String servername;
	private RegisteredServer server;
	
	public VelocityVisualServer(String id, String ip, int port){
		this.id = id;
		this.ip = ip;
		this.port = port;
		VelocityMain.logging(Level.INFO, "[VisualServer] IP: " + ip + ", PORT: " + port);
		Optional<RegisteredServer> opt = VelocityMain.getInstance().getServer().getAllServers().stream().filter(serv -> {
			InetSocketAddress address = serv.getServerInfo().getAddress();
			return address.getAddress().getHostAddress().equals(ip) && address.getPort() == port;
		}).findFirst();
		if(opt.isPresent()){
			this.server = opt.get();
			this.servername = server.getServerInfo().getName();
		}
	}
	
	public String getServerName(){
		return servername;
	}
	
	public RegisteredServer getServer(){
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
	
	public static VelocityVisualServer parsing(Channel ctx, String message){
		String[] sp = message.split(":");
		if(sp.length == 2){
			if(sp[0].equals("visualserver")){
				try{
					int port = Integer.parseInt(sp[1]);
					InetSocketAddress address = (InetSocketAddress) ctx.remoteAddress();
					return new VelocityVisualServer(ctx.id().asShortText(), address.getAddress().getHostAddress(), port);
				}catch(Exception e){
					return null;
				}
			}
		}
		return null;
	}
}
