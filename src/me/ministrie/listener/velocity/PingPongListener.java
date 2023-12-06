package me.ministrie.listener.velocity;

import java.util.logging.Level;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import me.ministrie.net.events.velocity.ReceivedClientMessageEvent;
import me.ministrie.net.impl.VelocityServer;
import me.ministrie.velocity.VelocityMain;

public class PingPongListener{

	@Subscribe
	public synchronized void onReceive(ReceivedClientMessageEvent event){
		RegisteredServer info = event.getSender();
		String messages = event.getMessages();
		VelocityMain.logging(Level.INFO, "[SocketFramework] receive to client. sender->" + info.getServerInfo().getName() + ", messages: " + messages);
		VelocityServer server = VelocityMain.getService().getServer(VelocityServer.class);
		if(server.getConnection(info) != null){
			server.getConnection(info).sendData("pong~~~!!!");
			VelocityMain.logging(Level.INFO, "[SocketFramework] send to response messages for client. messages: pong~~~!!!");
		}else{
			VelocityMain.logging(Level.WARNING, "[SocketFramework] failed send response messages. cause by: client connection is null");
		}
	}
}
