package me.ministrie.velocity;

import java.io.IOException;
import java.io.File;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.route.Route;

public class SocketConfig{
	public enum Config{
		IP("ip", "61.75.34.208"),
		PORT("port", 55105),
		DEBUG("debug", false);
		
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
		public <T> T getDefaultValue(){
			return (T) def_value;
		}
	}
	
	public static final String DIR = "./plugins/SocketFramework";
	public static final String FILENAME = "config.yml";
	
	private String ip;
	private int port;
	private boolean debug;
	
	public SocketConfig(String ip, int port, boolean debug){
		this.ip = ip;
		this.port = port;
		this.debug = debug;
	}
	
	public String getIP(){
		return ip;
	}
	
	public int getPort(){
		return port;
	}
	
	public boolean debug(){
		return debug;
	}
	
	public static SocketConfig load(){
		File file = new File(DIR, FILENAME);
		try{
			YamlDocument doc = YamlDocument.create(file);
			String ip = doc.getString(Route.from(Config.IP.getPath()));
			Integer port = doc.getInt(Route.from(Config.PORT.getPath()));
			Boolean debug = doc.getBoolean(Route.from(Config.DEBUG.getPath()));
			boolean save = false;
			if(!doc.contains(Route.from(Config.IP.getPath()))){
				ip = Config.IP.getDefaultValue();
				doc.set(Route.from(Config.IP.getPath()), Config.IP.getDefaultValue()); 
				save = true;
			}
			if(!doc.contains(Route.from(Config.PORT.getPath()))){
				port = Config.PORT.getDefaultValue();
				doc.set(Route.from(Config.PORT.getPath()), Config.PORT.getDefaultValue());
				save = true;
			}
			if(!doc.contains(Route.from(Config.DEBUG.getPath()))){
				debug = Config.DEBUG.getDefaultValue();
				doc.set(Route.from(Config.DEBUG.getPath()), Config.DEBUG.getDefaultValue());
				save = true;
			}
			if(save){
				doc.update();
				doc.save();
			}
			return new SocketConfig(ip, port, debug);
		}catch (IOException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
