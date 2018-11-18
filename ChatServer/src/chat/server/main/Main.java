package chat.server.main;

import java.net.BindException;
import java.net.InetSocketAddress;

import chat.common.handler.ChatCommonInboundPriorityHandler;
import chat.common.handler.ChatCommonPacketPrepender;
import chat.common.handler.ChatCommonPacketSplitter;
import chat.server.handler.AttributeSaver;
import chat.server.handler.ChatServerInboundHandler;
import chat.server.handler.ChatServerPacketDecoder;
import chat.server.handler.ChatServerPacketEncoder;
import chat.server.handler.InboundExceptionHandler;
import chat.server.play.Matchmaking;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Main {
	public static int PORT = 46435;

	public ChatServerInboundHandler handle = null;
	public ChatCommonInboundPriorityHandler priority = null;

	public Main(int port) throws Exception {
		// TODO Auto-generated constructor stub
		System.out.println("[Boot] Info: Starting Chat Server");
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup work = new NioEventLoopGroup();
		try {
			ServerBootstrap sb = new ServerBootstrap();
			sb.group(boss, work).channel(NioServerSocketChannel.class).localAddress(port)
					.childHandler(new ChannelInitializer<Channel>() {
						private InboundExceptionHandler ieh = new InboundExceptionHandler();

						@Override
						protected void initChannel(Channel ch) throws Exception {
							// TODO Auto-generated method stub
							System.out.println(
									"New connection from " + ((InetSocketAddress) ch.remoteAddress()).getHostName());
							handle = new ChatServerInboundHandler();
							priority = new ChatCommonInboundPriorityHandler();
							ch.pipeline().addLast("inboundsplit", new ChatCommonPacketSplitter())
									.addLast("outboundprepend", new ChatCommonPacketPrepender())
									.addLast("inboundpacketdecoder", new ChatServerPacketDecoder())
									.addLast("outboundpacketencoder", new ChatServerPacketEncoder())
									.addLast("inboundpriority", priority).addLast("inboundhandle", handle)
									.addLast("inboundexception", ieh);

							ch.attr(AttributeSaver.manager).set(new ChannelManager());
						}
					});

			Matchmaking.startThread(work);

			ChannelFuture f = sb.bind().sync();
			System.out.println("[Boot] Info: Started server");
			f.channel().closeFuture().sync();
		} finally {
			// TODO: handle finally clause
			boss.shutdownGracefully().syncUninterruptibly();
			work.shutdownGracefully().syncUninterruptibly();
		}
	}

	public static void main(String[] args) {
		try {
			new Main(PORT);
		} catch (BindException e) {
			System.out.println("[Boot] Error: Failed to bind to port! Maybe another server is running on this port?");
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e.getClass().getName() + ": " + e.getLocalizedMessage());
		}
	}
}
