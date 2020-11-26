package myrpc;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

public class RpcCallProxy implements InvocationHandler {

	private ActorRef rpcClientServer;

	private Class<?> clz;

	@SuppressWarnings("unchecked")
	public <T> T getProxy(ActorRef rpcClientServer, Class<T> clz) {
		this.rpcClientServer = rpcClientServer;
		this.clz = clz;
		Class<?>[] clzz = new Class<?>[] { clz };
		return (T) Proxy.newProxyInstance(clz.getClassLoader(), clzz, this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		RpcResponse result =new RpcResponse();
		RpcRequest request =new RpcRequest();
		request.setClassName(clz.getName());
		request.setMethodName(method.getName());
		request.setParameters(args);

		Future<Object> future = Patterns.ask(rpcClientServer, request,
				new Timeout(Duration.create(5, TimeUnit.SECONDS)));
		Object o = Await.result(future, Duration.create(5, TimeUnit.SECONDS));

		result = (RpcResponse)o;
		return result.getData();
	}


}