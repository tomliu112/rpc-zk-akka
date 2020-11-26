package myrpc.zk;

import myrpc.utils.SerializationUtil;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private ZooKeeperClient zkClient;

    public ZooKeeperServiceRegistry(ZooKeeperClient zkClient) {

        this.zkClient = zkClient;
    }

    @Override
    public void register(String serviceName) {
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH;
        try {
            if (zkClient.exists(registryPath) == null) {
                zkClient.createNode(registryPath, "root".getBytes());

                LOGGER.info("create registry node: {}", registryPath);
            }else{
                LOGGER.info(" node: {} already exist", registryPath);
            }
            // 创建 service 节点（持久）
            String servicePath = registryPath + "/" + serviceName;
            if (zkClient.exists(servicePath) == null) {
                zkClient.createNode(servicePath, "child1".getBytes());
                LOGGER.info("create service node: {}", servicePath);
            }else{
                LOGGER.info(" node: {} already exist", servicePath);
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void register1(Object service) {
        // 创建 registry 节点（持久）
        String registryPath = Constant.ZK_REGISTRY_PATH+"/test";
        try {
            if (zkClient.exists(registryPath) == null) {
                zkClient.createNode(registryPath, SerializationUtil.serialize(service));

                LOGGER.info("create registry node: {}", registryPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}