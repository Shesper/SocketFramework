package me.ministrie.net.impl;

import io.netty.channel.group.ChannelGroup;

public interface IServer{
	public ChannelGroup getRegisteredChannels();
}
