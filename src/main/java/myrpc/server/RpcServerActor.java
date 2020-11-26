package myrpc.server;

import akka.actor.UntypedActor;
import myrpc.RpcRequest;
import myrpc.RpcResponse;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class RpcServerActor extends UntypedActor {

	private Map<String, Object> proxyBeans;

	public RpcServerActor(Map<Class<?>, Object> beans) {
		proxyBeans = new HashMap<String, Object>();
		for (Iterator<Class<?>> iterator = beans.keySet().iterator(); iterator
				.hasNext();) {
			Class<?> inface = iterator.next();
			proxyBeans.put(inface.getName(), beans.get(inface));
		}
	}

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof RpcRequest) {
			RpcRequest request=(RpcRequest)message;
			Object serviceClass = proxyBeans.get(request.getClassName());
			Object[] params = request.getParameters();
			Class<?>[] paramerTypes = new Class<?>[params.length];
			if (params != null) {
				for (int i=0;i<params.length;i++) {
					paramerTypes[i]=params[i].getClass();
				}
			}
			//使用 jdk 执行反射调用
//			Method method = serviceClass.getClass().getMethod(request.getMethodName(),
//					paramerTypes);
//			Object o = method.invoke(serviceClass, params);

			//使用 CGLib 执行反射调用
			FastClass serviceFastClass = FastClass.create(serviceClass.getClass());
			FastMethod serviceFastMethod = serviceFastClass.getMethod(request.getMethodName(), paramerTypes);
			Object o =  serviceFastMethod.invoke(serviceClass, params);
			RpcResponse response=new RpcResponse();
			response.setData(o);
			System.out.println(this.getSelf().path());
			getSender().tell(response, getSelf());
		}else
			unhandled(message);
	}

}
