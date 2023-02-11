package me.ministrie.bungee;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;

public class SocketConfig{
	
	public enum Config{
		IP("config.connection.ip", "localhost"),
		PORT("config.connection.port", 55105),
		DEBUG("config.debug", false);
		
		private String path;
		private Object def_value;
		
		Config(String path, Object def){
			this.path = path;
			this.def_value = def;
		}
		
		public String getPath(){
			return path;
		}
		
		@SuppressWarnings("unchecked")
		public <T> T getValue(Configuration config){
			if(config == null){
				return (T) def_value;
			}else{
				return (T) config.get(path, def_value);
			}
		}
	}

	private String ip;
	private int port;
	private boolean debug;
	
	public SocketConfig(File file, ConfigurationProvider provider) throws IOException{
		boolean save = false;
		Configuration config = provider.load(file);
		for(Config cfg : Config.values()){
			if(!config.contains(cfg.getPath())){
				config.set(cfg.getPath(), cfg.getValue(null));
				save = true;
			}
		}
		this.ip = Config.IP.getValue(config);
		this.port = Config.PORT.getValue(config);
		this.debug = Config.DEBUG.getValue(config);
		if(save) provider.save(config, file);
	}
	
	public String getIP(){
		if(ip == null || ip.isEmpty()) return Config.IP.getValue(null);
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public boolean debug(){
		return debug;
	}
}
