package me.ministrie.listener.bukkit;

import java.util.logging.Level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.ministrie.bukkit.BukkitMain;
import me.ministrie.net.events.ReceivedServerMessageEvent;

public class PingPongListener implements Listener{
	
	@EventHandler
	public synchronized void onReceive(ReceivedServerMessageEvent event){
		String messages = event.getMessages();
		BukkitMain.logging(Level.INFO, "[SocketFramework] receive to server. messages: " + messages);
	}
}
