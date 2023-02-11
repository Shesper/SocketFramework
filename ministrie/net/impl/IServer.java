package me.ministrie.net.impl;

import io.netty.channel.group.ChannelGroup;
import net.md_5.bungee.api.config.ServerInfo;

public interface IServer{
	public ServiceHandler getConnection(ServerInfo info);
	public ChannelGroup getRegisteredChannels();
}
