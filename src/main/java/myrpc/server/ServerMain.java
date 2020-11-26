package myrpc.server;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import myrpc.services.Hello;
import myrpc.services.Rzk;
import myrpc.zk.ZooKeeperClient;
import myrpc.zk.ZooKeeperServiceRegistry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ServerMain {

	public static void main(String[] args) {
		final Config config = ConfigFactory
				.parseString("akka.remote.netty.tcp.port=" + 2551)
				.withFallback(
						ConfigFactory
								.parseString("akka.cluster.roles = [RpcServer1]"))
				.withFallback(
						ConfigFactory
								.parseString("akka.remote.netty.tcp.hostname=127.0.0.1"))
				.withFallback(ConfigFactory.load());

		ActorSystem system = ActorSystem.create("EsbSystem", config);
		
		// Server 加入发布的服务
		Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();
		services.put(Rzk.class, new Hello());
		//新建RpcServerActor,并且加入集群,rpcServer1与AkkaRpcClient中对应routeesPaths
		system.actorOf(Props.create(RpcServerActor.class, services), "rpcServer1");
		system.actorOf(Props.create(RpcServerActor.class, services), "rpcServer2");
		system.actorOf(Props.create(RpcServerActor.class, services), "rpcServer3");
		try {

			ZooKeeperClient zkClient = new ZooKeeperClient("localhost:2181",null,null);
			ZooKeeperServiceRegistry registry = new ZooKeeperServiceRegistry(zkClient);
			registry.register("rpcServer1");
			registry.register("rpcServer2");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}