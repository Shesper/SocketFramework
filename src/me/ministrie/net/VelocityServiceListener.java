package me.ministrie.net;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.logging.Level;

import com.velocitypowered.api.proxy.server.RegisteredServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.ministrie.net.events.velocity.ReceivedClientMessageEvent;
import me.ministrie.utils.VelocityVisualServer;
import me.ministrie.velocity.VelocityMain;

public class VelocityServiceListener extends SimpleChannelInboundHandler<ByteBuf>{
	
	private VelocityEchoServer server;
	
	public VelocityServiceListener(VelocityEchoServer server){
		this.server = server;
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
		InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
		VelocityMain.logging(Level.FINE, "[SocketFramework] registered channel. ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", channelID->" + ctx.channel().id().asShortText());
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
		RegisteredServer server = this.server.getServer(ctx.channel());
		if(server != null){
			this.server.ejectHandler(server, ctx);
			VelocityMain.logging(Level.INFO, "[SocketFramework] lost connection by " + server.getServerInfo().getName());
		}else{
			InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
			VelocityMain.logging(Level.WARNING, "[SocketFramework] unknown server disconnected! ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort());
		}
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
		// TODO Auto-generated method stub
		RegisteredServer server = this.server.getServer(ctx.channel());
		String messages = buffer.toString(Charset.forName("UTF-8"));
		
		VelocityMain.logging(Level.INFO, "[Incomming Data] incomming message->" + messages + ", server: " + (server != null ? server.getServerInfo().getName() : "null"));
		VelocityVisualServer visual = VelocityVisualServer.parsing(ctx.channel(), messages);
		
		if(visual != null){
			if(visual.getServer() != null){
				if(this.server.getConnection(visual.getServer()) != null){
					VelocityMain.logging(Level.WARNING, "[SocketFramework] already connection by " + server.getServerInfo().getName());
					ctx.close();
					return;
				}
				this.server.registServer(visual);
				this.server.registHandler(visual.getServer(), new VelocityHandler(ctx));
				VelocityMain.logging(Level.INFO, "[SocketFramework] registered server. channelID->" + visual.getID() + ", servername: " + visual.getServerName());
			}else{
				InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
				VelocityMain.logging(Level.WARNING, "[SocketFramework] unknown server connection! ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", messages: " + messages);
				ctx.close();
				return;
			}
			return;
		}
		if(server != null){
			VelocityMain.getInstance().getServer().getEventManager().fireAndForget(new ReceivedClientMessageEvent(server, messages));
			VelocityMain.logging(Level.INFO, "[Incomming Data] call event.");
		}else{
			InetSocketAddress adr = (InetSocketAddress) ctx.channel().remoteAddress();
			VelocityMain.logging(Level.WARNING, "[SocketFramework] incomming from unknown server. ip->" + adr.getAddress().getHostAddress() + ":" + adr.getPort() + ", messages: " + messages);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
		cause.printStackTrace();
		ctx.close();
	}
}
