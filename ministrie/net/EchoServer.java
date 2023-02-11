package me.ministrie.net;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatchers;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.ministrie.bungee.BungeeMain;
import me.ministrie.net.impl.IClient;
import me.ministrie.net.impl.IServer;
import me.ministrie.net.impl.NioService;
import me.ministrie.net.impl.ServerHandler;
import me.ministrie.net.impl.ServiceHandler;
import me.ministrie.utils.VisualServer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class EchoServer implements NioService, IServer{
	
	private String ip;
	private int port;
	private Thread serverthread;
	private final ChannelGroup allChannels = new DefaultChannelGroup("server", GlobalEventExecutor.INSTANCE);
    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;
    private ServerBootstrap bootstrap;
    private boolean started;
    private boolean close;
    private Map<String, ServerHandler> handlers = new HashMap<>();
    private Map<String, ServerInfo> servers = new HashMap<>();
    public static final char end_of_char = ';';
    private byte[] end_of_byte = {(byte)end_of_char};
    
    
    public EchoServer(){
    	bossEventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        workerEventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("worker"));
        
        // Boss Thread는 ServerSocket을 Listen
        // Worker Thread는 만들어진 Channel에서 넘어온 이벤트를 처리
        bootstrap = new ServerBootstrap();

        bootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        	@Override
        	protected void initChannel(SocketChannel socketChannel) throws Exception {
        		ByteBuf delimiter = Unpooled.wrappedBuffer(end_of_byte);
        		ChannelPipeline pipeline = socketChannel.pipeline();
        		// 핸들러 설정
        		//pipeline.addLast(new StringDecoder(Charset.defaultCharset()));
        		//pipeline.addLast(new StringEncoder(Charset.defaultCharset()));
        		pipeline.addLast(new DelimiterBasedFrameDecoder(16383, delimiter));
        		pipeline.addLast(new ServiceListener(EchoServer.this));
        	}
        });

        // Channel 생성시 사용할 클래스 (NIO 소켓을 이용한 채널)
        bootstrap.channel(NioServerSocketChannel.class);

        // accept 되어 생성되는 TCP Channel 설정
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
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
    	serverthread = new Thread(() -> {
    		BungeeCord.getInstance().getLogger().log(Level.INFO, "[SocketFramework] Listening for netty connections on port " + BungeeMain.config.getPort());
        	while(!close){
        		try{
                    // Channel 생성후 기다림
                    ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(ip, port)).sync();
                    // 클라이언트 도착! 채널 얻기.
                    Channel channel = bindFuture.channel();
                    allChannels.add(channel);
                    // Channel이 닫힐 때까지 대기
                    channel.closeFuture().sync();
        		}catch (InterruptedException e){
        			throw new RuntimeException(e);
        		}
        	}
    	});
    	serverthread.start();
    }
    
    @Override
    public void close(boolean restart){
    	close = true;
    	//handlers.entrySet().stream().forEach(e -> e.getValue().sendData("quit"));
        allChannels.close().awaitUninterruptibly();
        workerEventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        bossEventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        BungeeCord.getInstance().getLogger().log(Level.INFO, "[SocketFramework] echo server is closed.");
    }
    
    public static NioService getNewServer(){
    	return new EchoServer();
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
	public ServiceHandler getConnection(ServerInfo info){
		if(info == null) return null;
		return handlers.get(info.getName());
	}

	@Override
	public ChannelGroup getRegisteredChannels(){
		return allChannels;
	}
	
	@Override
	public IServer getServer(){
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public IClient getClient() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void registHandler(ServerInfo info, ServerHandler handler){
		if(handlers.containsKey(info.getName())){
			throw new IllegalArgumentException("already registered server handler.");
		}else{
			handlers.put(info.getName(), handler);
		}
	}
	
	public void registServer(VisualServer server){
		if(servers.containsKey(server.getID())){
			throw new IllegalArgumentException("already registered server info.");
		}else{
			servers.put(server.getID(), server.getServer());
		}
	}
	
	public ServerInfo getServer(Channel channel){
		return servers.get(channel.id().asShortText());
	}
	
	public void ejectHandler(ServerInfo info, ChannelHandlerContext ctx){
		handlers.remove(info.getName());
		allChannels.close(ChannelMatchers.is(ctx.channel()));
		servers.remove(ctx.channel().id().asShortText());
	}
}
