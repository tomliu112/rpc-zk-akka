package myrpc.zk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private ZooKeeperClient zkClient;
    public ZooKeeperServiceDiscovery(ZooKeeperClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public List<String> discover(String name) {
        // 创建 ZooKeeper 客户端


        List<String> servers = new ArrayList<>();
        List<String> addressList=null;
        try {

            LOGGER.debug("connect zookeeper");
            // 获取 service 节点
            String servicePath = Constant.ZK_REGISTRY_PATH + "/" + name;
//            if (zkClient.exists(servicePath) != null) {
//                LOGGER.error("can not find any service node on path: {}", servicePath);
//            }
             addressList = zkClient.getChildren(Constant.ZK_REGISTRY_PATH);
            if (addressList==null || addressList.size()==0) {
                LOGGER.error("can not find any service node on path: {}", servicePath);
            }

//            for(String addressPath:addressList){
//                servers.add(new String(zkClient.getData(addressPath)));
//            }

        } catch (Exception e){
            e.printStackTrace();
            LOGGER.error(e.toString());
        }
        finally {
            try {
                if (zkClient != null) {
                    zkClient.close();
                    LOGGER.info("Explictly closing ok");
                }
            } catch (Exception e) {
                LOGGER.error("zk client connection is expired, but failed to close, so fail over it");
            }
        }
        return addressList;
    }
}