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
import me.ministrie.net.impl.BungeeServer;
import me.ministrie.net.impl.IClient;
import me.ministrie.net.impl.IServer;
import me.ministrie.net.impl.NioService;
import me.ministrie.net.impl.ServerCase;
import me.ministrie.net.impl.ServerHandler;
import me.ministrie.net.impl.ServiceHandler;
import me.ministrie.utils.VisualServer;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class EchoServer implements NioService, IServer, BungeeServer{
	
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
        	//파이프라인. 클라이언트 측에서 서버로 패킷이 들어올 경우 이 메서드를 거치는거 같다.
        	//패킷의 구조는 패킷의 타입을 구분하는 헤드, 패킷을 구성하는 바디(몸통), 그리고 구분자로 마무리 하는게 일반적인것 같다.
        	//패킷의 끝을 알 수 있는 구분자를 설정하고 패킷을 읽어들여 처리하는 리스너를 추가한다.
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
    	//자바에서 기본적으로 제공하는 소켓 API를 토대로 제작했다.
    	//netty에서는 이런식으로 무한 루프를 돌려야하는지 그러지 않아도 되는지 아직 알 수 없는 상태지만
    	//이 상태로 테스트 해본 바 정상적으로 구동되었다.
    	//아마 비슷한 방식으로 클라이언트의 연결을 담당하는 것 같다. 아닐수도.
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
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getServer(Class<? extends ServerCase> adapter){
		// TODO Auto-generated method stub
		return (T) this;
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
