package me.ministrie.net.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReceivedServerMessageEvent extends Event{
	
    private static final HandlerList handlers = new HandlerList();
    
	private String messages;
	
	public ReceivedServerMessageEvent(String messages){
		super(true);
		this.messages = messages;
	}
	
	public String getMessages(){
		return messages;
	}

	@Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
