package com.test.rpc.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.test.rpc.common.RpcRequest;
import com.test.rpc.common.RpcResponse;

/**
 * RPC 处理器（用于处理 RPC 请求）
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

	private final Map<String, Object> handlerMap;

	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final RpcRequest request) throws Exception {
		// 多线程处理请求
		RpcServer.submit(new Runnable() {
			@Override
			public void run() {
				RpcResponse response = new RpcResponse();
				response.setRequestId(request.getRequestId());
				try {
					Object result = handle(request);
					response.setResult(result);
				} catch (Throwable t) {
					response.setError(t);
				}
				ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture channelFuture) throws Exception {
						LOGGER.debug("Send response for request " + request.getRequestId());
					}
				});
			}
		});
	}

	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();
		Object serviceBean = handlerMap.get(className);

		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();

		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
