package github.chenzhihui.remoting.transport;

import github.chenzhihui.extension.SPI;
import github.chenzhihui.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and get result
     *
     * @param rpcRequest message body
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}