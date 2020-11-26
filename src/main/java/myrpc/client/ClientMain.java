package myrpc.client;


import myrpc.services.Rzk;

public class ClientMain {

	public static void main(String[] args) {
		AkkaRpcClient client = AkkaRpcClient.getInstance();
		long start = System.currentTimeMillis();
		while (true) {
			Rzk example = client.getProxy(Rzk.class);
			long time = System.currentTimeMillis() - start;
			Object re = example.hi("rpc");
			System.out.println(re.toString()+" "+time);
		}
	}
}