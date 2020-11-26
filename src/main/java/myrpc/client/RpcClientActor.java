package myrpc.client;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;
import akka.util.Timeout;
import myrpc.RpcRequest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;


public class RpcClientActor extends UntypedActor {

	private ActorRef rpc;

	public RpcClientActor(ActorRef rpc) {
		this.rpc = rpc;
	}

	@Override
	public void onReceive(Object message) throws Exception {

		//System.out.println(message.getClass());
		if (message instanceof RpcRequest) {
			Future<Object> future = Patterns.ask(rpc, message, new Timeout(
					Duration.create(5, TimeUnit.SECONDS)));
			Object o = Await.result(future,
					Duration.create(5, TimeUnit.SECONDS));
			getSender().tell(o, getSelf());

		}else
			unhandled(message);
	}
}