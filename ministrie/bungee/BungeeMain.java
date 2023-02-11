package me.ministrie.bungee;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.mcstats.bungee.Metrics;

import me.ministrie.listener.bungee.PingPongListener;
import me.ministrie.net.EchoServer;
import me.ministrie.net.impl.NioService;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeMain extends Plugin{

	public static BungeeMain inst;
	public static NioService server;
	public static SocketConfig config;
	public static final int pluginID = 16746;
	
	@Override
	public void onEnable(){
		File file = new File(getDataFolder(), "config.yml");
		if(!file.exists()){
            if(!getDataFolder().exists()){
                if(!getDataFolder().mkdir()) throw new RuntimeException("[SocketFramework] failed create config folder. plugin disabled.");
            }
			try{
				if(!file.createNewFile()) throw new RuntimeException("[SocketFramework] failed create config file. plugin disabled.");
			}catch (IOException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try{
			config = new SocketConfig(file, ConfigurationProvider.getProvider(YamlConfiguration.class));
		}catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		server = EchoServer.getNewServer().setIP(config.getIP()).setPort(config.getPort());
		server.startService();
		@SuppressWarnings("unused")
		Metrics m = new Metrics(this, pluginID);
		inst = this;
		if(config.debug()){
			this.getProxy().getPluginManager().registerListener(this, new PingPongListener());
		}
	}
	
	@Override
	public void onDisable(){
		if(server != null) server.close(false);
	}
	
	public static NioService getService(){
		return server;
	}
	
	public static SocketConfig getConfig(){
		return config;
	}
	
	public static void logging(Level level, String log){
		if(config.debug()) inst.getLogger().log(level, log);
	}
}
