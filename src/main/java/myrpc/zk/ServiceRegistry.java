package myrpc.zk;

public interface ServiceRegistry {

    /**
     * 注册服务名称与服务地址
     *
     * @param serviceName    服务名称
     */
    void register(String serviceName);
}