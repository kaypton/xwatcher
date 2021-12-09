import com.alibaba.fastjson.JSON;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.XMessageBuilder;
import com.github.fenrir.xmessaging.rpc.types.RpcRequestMessage;
import com.github.fenrir.xmessaging.rpc.types.XMessagingRpcConstants;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RpcClientTest {

    public XMessage testCast(String server, String function, Class<?> returnType, Object... args){
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.dstServer = server;
        this.assembleParameters(requestMessage, args);
        requestMessage.dstFunction = function;
        requestMessage.returnTypeName = returnType.getTypeName();

        return XMessageBuilder.builder("Server")
                .addHeader(XMessagingRpcConstants.XMESSAGING_RPC_ROLE,
                        XMessagingRpcConstants.XMESSAGING_RPC_ROLE_REQUEST)
                .setStringPayload(JSON.toJSONString(requestMessage))
                .buildNatsMessage();
    }

    private void assembleParameters(RpcRequestMessage requestMessage, Object... args){
        List<String> parametersTypeName = new ArrayList<>();
        for(Object arg : args){
            parametersTypeName.add(arg.getClass().getTypeName());
        }
        requestMessage.parametersTypeName = parametersTypeName;
        requestMessage.parameters = Arrays.asList(args);
    }

    public String testAssembleFunctionID(String server, String function, Class<?> returnType, Object... args){
        RpcRequestMessage requestMessage = new RpcRequestMessage();
        requestMessage.dstServer = server;
        this.assembleParameters(requestMessage, args);
        requestMessage.dstFunction = function;
        requestMessage.returnTypeName = returnType.getTypeName();

        StringBuilder functionIDBuilder = new StringBuilder();
        functionIDBuilder.append(requestMessage.dstFunction)
                .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
        for(String parameterTypeName: requestMessage.parametersTypeName) {
            functionIDBuilder.append(parameterTypeName)
                    .append(XMessagingRpcConstants.XMESSAGING_RPC_FUNCTION_ID_SEPARATOR);
        }
        functionIDBuilder.append(requestMessage.returnTypeName);
        return functionIDBuilder.toString();
    }

    @Test
    public void testCast(){
        System.out.println(testCast("hello", "append", String.class, "a", 1)
            .getStringData());
        System.out.println(testAssembleFunctionID("hello", "append", String.class, "a", 1));
    }
}
