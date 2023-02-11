package me.ministrie.net;

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import me.ministrie.bukkit.BukkitMain;
import me.ministrie.net.events.ReceivedServerMessageEvent;
import me.ministrie.net.impl.NioService;
import me.ministrie.net.impl.ServiceHandler;

public class ClientListener extends SimpleChannelInboundHandler<ByteBuf> implements ServiceHandler{

	private ChannelHandlerContext ctx;
	private EchoClient client;
	
	public ClientListener(EchoClient client){
		this.client = client;
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
		this.ctx = null;
		if(BukkitMain.inst.isEnabled()){
			ctx.channel().eventLoop().schedule(new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					NioService sv = (NioService) client;
					BukkitMain.logging(Level.INFO, "[SocketFramework] try reconnect. ip->" + sv.getIP() + ":" + sv.getPort());
					client.getBootstarp().connect(sv.getIP(), sv.getPort());
					BukkitMain.logging(Level.INFO, "[SocketFramework] reconnected done.");
				}
			}, 5, TimeUnit.SECONDS);
			Bukkit.getLogger().log(Level.INFO, "[SocketFramework] lost conntection by bungeecord server. try reconnect after 5 seconds.");
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception{
	    this.ctx = ctx;
	    String format = "visualserver:" + Bukkit.getServer().getPort() + EchoClient.end_of_char;
		ByteBuf messageBuffer = Unpooled.buffer();
		messageBuffer.writeBytes(format.getBytes(Charset.forName("UTF-8")));
		this.ctx.writeAndFlush(messageBuffer);
		Bukkit.getLogger().log(Level.INFO, "[SocketFramework] connect to bungeecord successfully!");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception{
		Bukkit.getLogger().log(Level.INFO, "[SocketFramework] disconnect from bungeecord server.");
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception{
		// TODO Auto-generated method stub
		String messages = buffer.toString(Charset.forName("UTF-8"));
		Bukkit.getPluginManager().callEvent(new ReceivedServerMessageEvent(messages));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
	    cause.printStackTrace();
	    ctx.close();
	}

	@Override
	public void sendData(String messages){
		// TODO Auto-generated method stub
		if(ctx != null){
			messages = messages + EchoClient.end_of_char;
			ByteBuf messageBuffer = Unpooled.buffer();
			messageBuffer.writeBytes(messages.getBytes(Charset.forName("UTF-8")));
			ctx.writeAndFlush(messageBuffer);
			BukkitMain.logging(Level.INFO, "[SocketFramework] successfully send messages. messages->" + messages);
		}else{
			BukkitMain.logging(Level.WARNING, "[SocketFramework] channel not yet activate.");
		}
	}
}
