package myrpc.zk;


import myrpc.utils.StringUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: SimpleZooKeeperClient <br/>
 * Function: 封装zookeeper客户端
 *
 */
public class ZooKeeperClient {

    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperClient.class);

    protected static final int DEFAULT_SESSION_TIMEOUT_MILLS = Constant.ZK_SESSION_TIMEOUT;

    protected static final int CONNECTION_TIMEOUT_MILLS = Constant.ZK_CONNECTION_TIMEOUT;

    public static final String ZK_PATH_SEPARATOR = "/";

    protected ZooKeeper zooKeeper = null;

    private boolean isAuth = false;

    public ZooKeeperClient(String connectString, String auth, Watcher watcher)
            throws IOException, InterruptedException {
        this(connectString, watcher, auth, DEFAULT_SESSION_TIMEOUT_MILLS);
    }

    public ZooKeeperClient(String connectString, Watcher watcher, String auth,
                           int sessionTimeoutMills) throws IOException, InterruptedException {
        if (sessionTimeoutMills <= 0) {
            throw new RuntimeException("param is not valid");
        }
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zooKeeper = new ZooKeeper(connectString, DEFAULT_SESSION_TIMEOUT_MILLS, watchedEvent -> {
            if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                countDownLatch.countDown();
            }
        });
        if (StringUtil.isNotEmpty(auth)) {
            LOG.info("Zookeeper auth enabled!");
            isAuth = true;
            zooKeeper.addAuthInfo("digest", auth.getBytes());
        }
        countDownLatch.await(CONNECTION_TIMEOUT_MILLS, TimeUnit.MILLISECONDS);
    }

    public void close() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
        }
    }

    public boolean isPathValid(String path) {
        if (StringUtil.isEmpty(path) || !path.startsWith(ZK_PATH_SEPARATOR)
                || (path.length() > 1 && path.endsWith(ZK_PATH_SEPARATOR))) {
            return false;
        }
        return true;
    }

    public String createNode(String path, byte[] data) throws KeeperException, InterruptedException {
        if (!isPathValid(path) || data == null) {
            throw new InterruptedException("param is not valid");
        }
        return zooKeeper.create(path, data, getAcl(), CreateMode.PERSISTENT);
    }

    public String createSessionNode(String path, byte[] data) throws KeeperException,
            InterruptedException {
        if (!isPathValid(path) || data == null) {
            throw new RuntimeException("param is not valid");
        }
        return zooKeeper.create(path, data, getAcl(), CreateMode.EPHEMERAL);
    }

    public String createNodeForRecursive(String path, byte[] data) throws KeeperException,
            InterruptedException {
        return createRecursiveNode(path, data, false);
    }

    public String createSessionNodeForRecursive(String path, byte[] data) throws KeeperException,
            InterruptedException {
        return createRecursiveNode(path, data, true);
    }

    private String createRecursiveNode(String path, byte[] data, boolean isSession)
            throws KeeperException, InterruptedException {
        if (!isPathValid(path) || data == null) {
            throw new RuntimeException("param is not valid");
        }

        String[] pathArr = path.split(ZK_PATH_SEPARATOR);
        String childPath = pathArr[0];
        for (int i = 1; i < pathArr.length - 1; i++) {
            childPath = new StringBuilder(childPath).append(ZK_PATH_SEPARATOR).append(pathArr[i])
                    .toString();
            if (exists(childPath) == null) {
                try {
                    zooKeeper.create(childPath, "".getBytes(), getAcl(), CreateMode.PERSISTENT);
                } catch (NodeExistsException e) {
                    LOG.warn("zookeeper path is already exists - " + childPath);
                } catch (RuntimeException e) {
                    throw e;
                }
            }
        }

        return zooKeeper.create(path, data, getAcl(), isSession ? CreateMode.EPHEMERAL
                : CreateMode.PERSISTENT);
    }

    public byte[] getData(String path) throws KeeperException, InterruptedException {
        if (!isPathValid(path)) {
            throw new RuntimeException("param is not valid");
        }
        return zooKeeper.getData(path, true, null);
    }

    public Stat setData(String path, byte[] data) throws KeeperException, InterruptedException {
        if (!isPathValid(path) || data == null) {
            throw new RuntimeException("param is not valid");
        }
        return zooKeeper.setData(path, data, -1);
    }

    public void delete(String path) throws InterruptedException, KeeperException {
        if (!isPathValid(path)) {
            throw new RuntimeException("param is not valid");
        }
        zooKeeper.delete(path, -1);
    }

    public Stat exists(String path) throws KeeperException, InterruptedException {
        if (!isPathValid(path)) {
            throw new RuntimeException("param is not valid");
        }
        return zooKeeper.exists(path, true);
    }

    public List<String> getChildren(String path) throws KeeperException, InterruptedException {
        if (!isPathValid(path)) {
            throw new RuntimeException("param is not valid");
        }
        return zooKeeper.getChildren(path, true);
    }

    public States getState() {
        return zooKeeper.getState();
    }

    private List<ACL> getAcl() {
        if (isAuth) {
            return Ids.CREATOR_ALL_ACL;
        }
        return Ids.OPEN_ACL_UNSAFE;
    }

    private class ConnectedWatcher implements Watcher {

        private Watcher userWatcher;
        private CountDownLatch countDownLatch;

        public ConnectedWatcher(Watcher userWatcher, CountDownLatch countDownLatch) {
            this.userWatcher = userWatcher;
            this.countDownLatch = countDownLatch;
        }

        public void process(WatchedEvent event) {
            if (event.getState() == KeeperState.SyncConnected && countDownLatch.getCount() > 0) {
                // log.info("Receive watched event: KeeperState.SyncConnected means zookeeper well connected");
                countDownLatch.countDown();
            }
            if (userWatcher != null) {
                userWatcher.process(event);
            }
        }
    }

}
