package me.ministrie.net;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.velocitypowered.api.proxy.server.RegisteredServer;

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
import me.ministrie.net.impl.IClient;
import me.ministrie.net.impl.IServer;
import me.ministrie.net.impl.NioService;
import me.ministrie.net.impl.ServerCase;
import me.ministrie.net.impl.ServerHandler;
import me.ministrie.net.impl.ServiceHandler;
import me.ministrie.net.impl.VelocityServer;
import me.ministrie.utils.VelocityVisualServer;
import me.ministrie.velocity.VelocityMain;

public class VelocityEchoServer implements NioService, IServer, VelocityServer{
	
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
	private Map<String, RegisteredServer> servers = new HashMap<>();
	public static final char end_of_char = ';';
	private byte[] end_of_byte = {(byte)end_of_char};
    
    
    public VelocityEchoServer(){
    	bossEventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("boss"));
        workerEventLoopGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("worker"));
        
        // Boss Thread�� ServerSocket�� Listen
        // Worker Thread�� ������� Channel���� �Ѿ�� �̺�Ʈ�� ó��
        bootstrap = new ServerBootstrap();

        bootstrap.group(bossEventLoopGroup, workerEventLoopGroup);
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
        	//����������. Ŭ���̾�Ʈ ������ ������ ��Ŷ�� ���� ��� �� �޼��带 ��ġ�°� ����.
        	//��Ŷ�� ������ ��Ŷ�� Ÿ���� �����ϴ� ���, ��Ŷ�� �����ϴ� �ٵ�(����), �׸��� �����ڷ� ������ �ϴ°� �Ϲ����ΰ� ����.
        	//��Ŷ�� ���� �� �� �ִ� �����ڸ� �����ϰ� ��Ŷ�� �о�鿩 ó���ϴ� �����ʸ� �߰��Ѵ�.
        	@Override
        	protected void initChannel(SocketChannel socketChannel) throws Exception {
        		ByteBuf delimiter = Unpooled.wrappedBuffer(end_of_byte);
        		ChannelPipeline pipeline = socketChannel.pipeline();
        		// �ڵ鷯 ����
        		//pipeline.addLast(new StringDecoder(Charset.defaultCharset()));
        		//pipeline.addLast(new StringEncoder(Charset.defaultCharset()));
        		pipeline.addLast(new DelimiterBasedFrameDecoder(16383, delimiter));
        		pipeline.addLast(new VelocityServiceListener(VelocityEchoServer.this));
        	}
        });

        // Channel ������ ����� Ŭ���� (NIO ������ �̿��� ä��)
        bootstrap.channel(NioServerSocketChannel.class);

        // accept �Ǿ� �����Ǵ� TCP Channel ����
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
    	//�ڹٿ��� �⺻������ �����ϴ� ���� API�� ���� �����ߴ�.
    	//netty������ �̷������� ���� ������ �������ϴ��� �׷��� �ʾƵ� �Ǵ��� ���� �� �� ���� ��������
    	//�� ���·� �׽�Ʈ �غ� �� ���������� �����Ǿ���.
    	//�Ƹ� ����� ������� Ŭ���̾�Ʈ�� ������ ����ϴ� �� ����. �ƴҼ���.
    	serverthread = new Thread(() -> {
    		VelocityMain.logging(Level.INFO, "[SocketFramework] Listening for netty connections on port " + VelocityMain.getInstance().getConfig().getPort());
        	while(!close){
        		try{
                    // Channel ������ ��ٸ�
                    ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(ip, port)).sync();
                    // Ŭ���̾�Ʈ ����! ä�� ���.
                    Channel channel = bindFuture.channel();
                    allChannels.add(channel);
                    // Channel�� ���� ������ ���
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
        VelocityMain.logging(Level.INFO, "[SocketFramework] echo server is closed.");
    }
    
    public static NioService getNewServer(){
    	return new VelocityEchoServer();
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
	public ServiceHandler getConnection(RegisteredServer server){
		if(server == null) return null;
		return handlers.get(server.getServerInfo().getName());
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
	
	public void registHandler(RegisteredServer server, ServerHandler handler){
		if(handlers.containsKey(server.getServerInfo().getName())){
			throw new IllegalArgumentException("already registered server handler.");
		}else{
			handlers.put(server.getServerInfo().getName(), handler);
		}
	}
	
	public void registServer(VelocityVisualServer server){
		if(servers.containsKey(server.getID())){
			throw new IllegalArgumentException("already registered server info.");
		}else{
			servers.put(server.getID(), server.getServer());
		}
	}
	
	public RegisteredServer getServer(Channel channel){
		return servers.get(channel.id().asShortText());
	}
	
	public void ejectHandler(RegisteredServer server, ChannelHandlerContext ctx){
		handlers.remove(server.getServerInfo().getName());
		allChannels.close(ChannelMatchers.is(ctx.channel()));
		servers.remove(ctx.channel().id().asShortText());
	}
}
