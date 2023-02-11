package me.ministrie.net;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Level;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.ministrie.bungee.BungeeMain;
import me.ministrie.net.events.ReceivedClientMessageEvent;
import me.ministrie.utils.VisualServer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class ServiceListener extends SimpleChannelInboundHandler<ByteBuf>{
	
	private EchoServer server;
	
	public ServiceListener(EchoServer server){
		this.server = server;
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
		InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
		BungeeMain.logging(Level.FINE, "[SocketFramework] registered channel. ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", channelID->" + ctx.channel().id().asShortText());
	}
	
	/*
	@SuppressWarnings("deprecation")
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception{
		InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
		Optional<Entry<String, ServerInfo>> result = BungeeCord.getInstance().getServers().entrySet().stream().filter(server -> server.getValue().getAddress().getAddress().getHostAddress().equals(adr.getAddress().getHostAddress())).findFirst();
		if(result.isPresent()){
			server.registHandler(result.get().getValue(), new Handler(ctx));
			BungeeMain.logging(Level.INFO, "[SocketFramework] connected client. ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", servername: " + result.get().getValue().getName());
		}else{
			BungeeMain.logging(Level.WARNING, "[SocketFramework] unknown server conntection! ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort());
		}
	}*/

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
		ServerInfo server = this.server.getServer(ctx.channel());
		if(server != null){
			this.server.ejectHandler(server, ctx);
			BungeeMain.logging(Level.INFO, "[SocketFramework] lost connection by " + server.getName());
		}else{
			InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
			BungeeMain.logging(Level.WARNING, "[SocketFramework] unknown server disconnected! ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort());
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		// TODO Auto-generated method stub
		ServerInfo server = this.server.getServer(ctx.channel());
		String messages = buffer.toString(Charset.forName("UTF-8"));
		
		BungeeMain.logging(Level.INFO, "[Incomming Data] incomming message->" + messages + ", server: " + (server != null ? server.getName() : "null"));
		VisualServer visual = VisualServer.parsing(ctx.channel(), messages);
		
		if(visual != null){
			if(visual.getServer() != null){
				if(this.server.getConnection(visual.getServer()) != null){
					BungeeMain.logging(Level.WARNING, "[SocketFramework] already connection by " + server.getName());
					ctx.close();
					return;
				}
				this.server.registServer(visual);
				this.server.registHandler(visual.getServer(), new Handler(ctx));
				BungeeMain.logging(Level.INFO, "[SocketFramework] registered server. channelID->" + visual.getID() + ", servername: " + visual.getServerName());
			}else{
				InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
				BungeeMain.logging(Level.WARNING, "[SocketFramework] unknown server connection! ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", messages: " + messages);
				ctx.close();
				return;
			}
			return;
		}
		if(server != null){
			BungeeCord.getInstance().getPluginManager().callEvent(new ReceivedClientMessageEvent(server, messages));
			BungeeMain.logging(Level.INFO, "[Incomming Data] call event.");
		}else{
			InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
			BungeeMain.logging(Level.WARNING, "[SocketFramework] incomming from unknown server. ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", messages: " + messages);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
		cause.printStackTrace();
		ctx.close();
	}
}
