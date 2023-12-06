package me.ministrie.velocity;

import java.util.logging.Level;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import me.ministrie.listener.velocity.PingPongListener;
import me.ministrie.net.VelocityEchoServer;
import me.ministrie.net.impl.NioService;

@Plugin(id = "SocketFramework", name = "SocketFramework", version = "1.1.7", authors = {"√÷≈¬º∫"})
public class VelocityMain{

	public static final int pluginID = 16746;
	public static NioService iserver;
	
	public static VelocityMain instance;
	
	private final ProxyServer server;
	private final Logger logger;
	private final SocketConfig config;
	
	@Inject
	public VelocityMain(ProxyServer server, Logger logger){
		VelocityMain.instance = this;
		this.server = server;
		this.logger = logger;
		this.config = SocketConfig.load();
	}
	
	public ProxyServer getServer(){
		return server;
	}
	
	public Logger getLogger(){
		return logger;
	}
	
	@Subscribe(order = PostOrder.FIRST)
	public void onEnable(ProxyInitializeEvent event){
		if(config.debug()) this.server.getEventManager().register(this, new PingPongListener());
		iserver = VelocityEchoServer.getNewServer().setIP(config.getIP()).setPort(config.getPort());
		iserver.startService();
	}
	
	@Subscribe(order = PostOrder.LAST)
	public void onDisable(ProxyShutdownEvent event){
		if(iserver != null) iserver.close(false);
	}
	
	public static VelocityMain getInstance(){
		return instance;
	}
	
	public static NioService getService(){
		return iserver;
	}
	
	public SocketConfig getConfig(){
		return config;
	}
	
	public static void logging(Level level, String log){
		if(VelocityMain.getInstance().getConfig().debug()){
			if(level.equals(Level.INFO)){
				VelocityMain.getInstance().getLogger().info(log);
			}else if(level.equals(Level.WARNING)){
				VelocityMain.getInstance().getLogger().warn(log);
			}else if(level.equals(Level.SEVERE)){
				VelocityMain.getInstance().getLogger().error(log);
			}
		}
	}
}
