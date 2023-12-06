package me.ministrie.net.impl;

import net.md_5.bungee.api.config.ServerInfo;

public interface BungeeServer extends ServerCase{
	public ServiceHandler getConnection(ServerInfo info);
}
