package me.ministrie.net;

import java.util.logging.Level;

import org.bukkit.Bukkit;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import me.ministrie.bukkit.BukkitMain;
import me.ministrie.net.impl.IClient;
import me.ministrie.net.impl.IServer;
import me.ministrie.net.impl.NioService;
import me.ministrie.net.impl.ServiceHandler;

public class EchoClient implements NioService, IClient{
	private String ip;
	private int port;
	private EventLoopGroup group;
	private Bootstrap bootstrap;
	private Thread client_thread;
	private boolean started;
	private ClientListener clientHandler = null;
    public static final char end_of_char = ';';
    private byte[] end_of_byte = {(byte)end_of_char};
	
	public EchoClient(){
		group = new NioEventLoopGroup();
		bootstrap = new Bootstrap();
		bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {
				protected void initChannel(SocketChannel socketChannel) throws Exception {
	        		ChannelPipeline pipeline = socketChannel.pipeline();
	        		//pipeline.addLast(new StringDecoder(Charset.defaultCharset()));
	        		//pipeline.addLast(new StringEncoder(Charset.defaultCharset()));
	        		ByteBuf delimiter = Unpooled.wrappedBuffer(end_of_byte);
	        		pipeline.addLast(new DelimiterBasedFrameDecoder(16383, delimiter));
					pipeline.addLast((clientHandler = new ClientListener(EchoClient.this)));
				}
			});
	}
	
	@Override
	public String getIP(){
		return ip;
	}
	
	@Override
	public int getPort(){
		return port;
	}
	
	@Override
	public void startService(){
		if(started) return;
		started = true;
		// TODO Auto-generated method stub
		client_thread = new Thread(() -> {
			try{
				ChannelFuture future = bootstrap.connect(ip, port).sync();
				future.channel().closeFuture().sync();
			}catch (InterruptedException e){
				// TODO Auto-generated catch block
				e.printStackTrace();
			}catch (Exception e){
				Bukkit.getLogger().log(Level.WARNING, "[SocketFramework] bungeecord server connection error. cause by: " + e.getLocalizedMessage());
			}
		});
		client_thread.start();
	}
	
	public Bootstrap getBootstarp(){
		return bootstrap;
	}

	@Override
	public NioService setIP(String ip) {
		// TODO Auto-generated method stub
		this.ip = ip;
		return this;
	}

	@Override
	public NioService setPort(int port) {
		// TODO Auto-generated method stub
		this.port = port;
		return this;
	}

	@Override
	public void close(boolean restart){
		// TODO Auto-generated method stub
		if(group != null){
			group.shutdownGracefully();
		}
		Bukkit.getLogger().log(Level.INFO, "[SocketFramework] close connection.");
		started = false;
		if(restart) Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitMain.inst, () -> startService(), 100);
	}

	@Override
	public IServer getServer(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ServiceHandler getConnection(){
		// TODO Auto-generated method stub
		return clientHandler;
	}

	@Override
	public IClient getClient(){
		// TODO Auto-generated method stub
		return this;
	}
	
	public static NioService getNewClient(){
		return new EchoClient();
	}
}
