package me.ministrie.bukkit;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

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
		public <T> T getValue(YamlConfiguration config){
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
	
	public SocketConfig(File file, YamlConfiguration config){
		boolean save = false;
		for(Config cfg : Config.values()){
			if(!config.isSet(cfg.getPath())){
				config.set(cfg.getPath(), cfg.getValue(null));
				save = true;
			}
		}
		this.ip = Config.IP.getValue(config);
		this.port = Config.PORT.getValue(config);
		this.debug = Config.DEBUG.getValue(config);
		
		if(save){
			try {
				config.save(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
