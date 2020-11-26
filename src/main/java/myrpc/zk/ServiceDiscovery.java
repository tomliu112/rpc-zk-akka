package myrpc.zk;

import java.util.List;

public interface ServiceDiscovery {

    /**
     * 根据服务名称查找服务地址
     *
     * @param serviceName 服务名称
     * @return 服务地址
     */
    List<String> discover(String serviceName);
}