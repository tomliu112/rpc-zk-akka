package myrpc.client;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.routing.AdaptiveLoadBalancingGroup;
import akka.cluster.routing.ClusterRouterGroup;
import akka.cluster.routing.ClusterRouterGroupSettings;
import akka.cluster.routing.HeapMetricsSelector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import myrpc.RpcCallProxy;
import myrpc.zk.ZooKeeperClient;
import myrpc.zk.ZooKeeperServiceDiscovery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AkkaRpcClient {

	private ActorSystem system;

	private ActorRef rpc;

	private ActorRef clientServer;

	private static AkkaRpcClient instance = null;

	public AkkaRpcClient() {
		final Config config = ConfigFactory
				.parseString("akka.remote.netty.tcp.port=" + 2552)
				.withFallback(
						ConfigFactory
								.parseString("akka.cluster.roles = [RpcClient]"))
				.withFallback(ConfigFactory.load());
		system = ActorSystem.create("RpcSystem", config);

		int totalInstances = 100;
		List<String> paths=null;
		try {
			ZooKeeperClient zkClient = new ZooKeeperClient("localhost:2181",null,null);
			ZooKeeperServiceDiscovery discovery = new ZooKeeperServiceDiscovery(zkClient);
			paths = discovery.discover("root");

		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> routeesPaths=new ArrayList<>();
		for(String path:paths){
			routeesPaths.add("/user/"+path);
		}
//		 = Arrays.asList("/user/rpcServer2","/user/rpcServer1");
		boolean allowLocalRoutees = false;
		ClusterRouterGroup clusterRouterGroup = new ClusterRouterGroup(
				new AdaptiveLoadBalancingGroup(
						HeapMetricsSelector.getInstance(),
						Collections.<String> emptyList()),
				new ClusterRouterGroupSettings(totalInstances, routeesPaths,
						allowLocalRoutees, "RpcServer1"));
		rpc = system.actorOf(clusterRouterGroup.props(), "rpcCall");
		clientServer = system.actorOf(Props.create(RpcClientActor.class, rpc),
				"rpcClient");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Cluster.get(system).registerOnMemberUp(new Runnable() {
			@Override
			public void run() {
				synchronized (instance) {
					System.out.println("notify");
					instance.notify();
				}
			}
		});
	}

	public static AkkaRpcClient getInstance() {
		if (instance == null) {
			instance = new AkkaRpcClient();
			synchronized (instance) {
				try {
					System.out.println("wait");
					instance.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		return instance;
	}
	public <T> T getProxy(Class<T> clz) {
		return new RpcCallProxy().getProxy(clientServer, clz);
	}

}