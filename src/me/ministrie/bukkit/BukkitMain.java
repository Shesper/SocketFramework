package me.ministrie.bukkit;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.bukkit.Metrics;

import me.ministrie.commands.bukkit.PingPongCommand;
import me.ministrie.listener.bukkit.PingPongListener;
import me.ministrie.net.EchoClient;
import me.ministrie.net.impl.NioService;

public class BukkitMain extends JavaPlugin{

	public static Plugin inst;
	public static NioService client;
	public static final String path = "./plugins/SocketFramework/config.yml";
	public static SocketConfig config;
	public static final int pluginID = 16746;
	
	@Override
	public void onEnable(){
		inst = this;
		loadConfig();
		client = EchoClient.getNewClient().setIP(config.getIP()).setPort(config.getPort());
		client.startService();
		@SuppressWarnings("unused")
		Metrics m = new Metrics(this, pluginID);
		
		if(config.debug()){
			this.getCommand("sendping").setExecutor(new PingPongCommand());
			Bukkit.getPluginManager().registerEvents(new PingPongListener(), this);
		}
	}
	
	@Override
	public void onDisable(){
		if(client != null) client.close(false);
	}
	
	public static NioService getService(){
		return client;
	}
	
	public static void loadConfig(){
		File f = new File(path);
		YamlConfiguration c = YamlConfiguration.loadConfiguration(f);
		config = new SocketConfig(f, c);
	}
	
	public static void logging(Level level, String log){
		if(config.debug()) inst.getLogger().log(level, log);
	}
}
