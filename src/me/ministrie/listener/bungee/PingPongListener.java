package me.ministrie.listener.bungee;

import java.util.logging.Level;

import me.ministrie.bungee.BungeeMain;
import me.ministrie.net.events.ReceivedClientMessageEvent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import me.ministrie.net.impl.BungeeServer;

public class PingPongListener implements Listener{
	
	@EventHandler
	public synchronized void onReceive(ReceivedClientMessageEvent event){
		ServerInfo info = event.getSender();
		String messages = event.getMessages();
		BungeeMain.logging(Level.INFO, "[SocketFramework] receive to client. sender->" + info.getName() + ", messages: " + messages);
		BungeeServer server = BungeeMain.getService().getServer(BungeeServer.class);
		if(server.getConnection(info) != null){
			server.getConnection(info).sendData("pong~~~!!!");
			BungeeMain.logging(Level.INFO, "[SocketFramework] send to response messages for client. messages: pong~~~!!!");
		}else{
			BungeeMain.logging(Level.WARNING, "[SocketFramework] failed send response messages. cause by: client connection is null");
		}
	}
}
