package me.ministrie.net.events.velocity;

import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ReceivedClientMessageEvent{

	private RegisteredServer sender;
	private String messages;
	
	public ReceivedClientMessageEvent(RegisteredServer sender, String messages){
		this.sender = sender;
		this.messages = messages;
	}
	
	public RegisteredServer getSender(){
		return sender;
	}
	
	public String getMessages(){
		return messages;
	}
}