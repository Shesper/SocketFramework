package me.ministrie.net.events;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Event;

public class ReceivedClientMessageEvent extends Event{

	private ServerInfo sender;
	private String messages;
	
	public ReceivedClientMessageEvent(ServerInfo sender, String messages){
		this.sender = sender;
		this.messages = messages;
	}
	
	public ServerInfo getSender(){
		return sender;
	}
	
	public String getMessages(){
		return messages;
	}
}
