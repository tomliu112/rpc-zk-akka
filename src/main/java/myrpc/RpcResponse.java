package myrpc;

import java.io.Serializable;

/**
 * 相应对象
 */
public class RpcResponse implements Serializable {

    //返回状态，当然这里可以，加入对应的异常等（这里简化）
    private String status;
    //返回的数据
    private Object data;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
