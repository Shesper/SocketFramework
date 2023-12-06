package me.ministrie.net;

import java.nio.charset.Charset;
import java.util.logging.Level;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import me.ministrie.bungee.BungeeMain;
import me.ministrie.net.impl.ServerHandler;

public class Handler implements ServerHandler{

	private ChannelHandlerContext ctx;
	
	public Handler(ChannelHandlerContext ctx){
		this.ctx = ctx;
	}
	
	@Override
	public void sendData(String messages){
		// TODO Auto-generated method stub
		if(ctx != null){
			messages = messages + EchoServer.end_of_char;
			ByteBuf messageBuffer = Unpooled.buffer();
			messageBuffer.writeBytes(messages.getBytes(Charset.forName("UTF-8")));
			ctx.writeAndFlush(messageBuffer);
			BungeeMain.logging(Level.INFO, "[SocketFramework] successfully send messages. messages->" + messages);
		}else{
			BungeeMain.logging(Level.WARNING, "[SocketFramework] channel not yet activate.");
		}
	}
}
