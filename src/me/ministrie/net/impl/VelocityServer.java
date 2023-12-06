package me.ministrie.net.impl;

import com.velocitypowered.api.proxy.server.RegisteredServer;

public interface VelocityServer extends ServerCase{
	public ServiceHandler getConnection(RegisteredServer server);
}
