package com.wsg.protocl_7001;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class TmServer {

	@Autowired
	private MessageHandler messageHandler;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void bind() {
		System.out.println("7001");
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup(4);
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new IdleStateHandler(60, 0, 0));
							ch.pipeline().addLast(new ProtocolCodec(),messageHandler);
							ch.config().setReuseAddress(true);
							ch.config().setReceiveBufferSize(2048);
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);
			b.bind(8001).sync();
		} catch (Throwable cause) {
			logger.error("E3G 7001 TM start exception", cause);
			relase();
		}




//		EventLoopGroup boss = new NioEventLoopGroup();
//		EventLoopGroup worker = new NioEventLoopGroup();
//		try {
//			ServerBootstrap server = new ServerBootstrap();
//			server.group(boss, worker).channel(NioServerSocketChannel.class)
//					.childHandler(new ChannelInitializer<Channel>() {
//						@Override
//						protected void initChannel(Channel ch) throws Exception {
//							ch.pipeline().addLast(new ProtocolCodec(),messageHandler);
//						}
//
//					});
//
//			ChannelFuture sync = server.bind(8001);
//			sync.channel().closeFuture().sync();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		} finally {
//			boss.shutdownGracefully();
//			worker.shutdownGracefully();
//		}

	}

	private void relase() {
		try {
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
				bossGroup.terminationFuture().sync();
			}
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
				workerGroup.terminationFuture().sync();
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}

	@PreDestroy
	public void stop() {
		relase();
	}

}
