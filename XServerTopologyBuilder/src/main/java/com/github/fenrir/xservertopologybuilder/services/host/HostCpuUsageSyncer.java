package com.github.fenrir.xservertopologybuilder.services.host;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xcommon.message.stream.SystemCpuSystemUtilStream;
import com.github.fenrir.xcommon.message.stream.SystemCpuTotalUtilStream;
import com.github.fenrir.xcommon.message.stream.SystemCpuUserUtilStream;
import com.github.fenrir.xfunnel.XFunnel;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessaging;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HostCpuUsageSyncer {

    private static class SystemCpuUsageStreamListener implements MessageProcessCallBack {
        @Getter @Setter private HostTopologyService service;
        public SystemCpuUsageStreamListener(HostTopologyService service){
            this.service = service;
        }

        @Override
        public void processMessage(XMessage data){
            JSONObject _data = JSON.parseObject(data.getStringData());
            service.report(
                    _data.getJSONObject("hostInfo").getString("host"),
                    "cpu.system.usage",
                    _data.getJSONObject("value").getDoubleValue("cpu.system.usage")
            );

            service.report(
                    _data.getJSONObject("hostInfo").getString("host"),
                    "cpu.total.usage",
                    _data.getJSONObject("value").getDoubleValue("cpu.total.usage")
            );

            service.report(
                    _data.getJSONObject("hostInfo").getString("host"),
                    "cpu.user.usage",
                    _data.getJSONObject("value").getDoubleValue("cpu.user.usage")
            );
        }
    }

    public HostCpuUsageSyncer(@Autowired HostTopologyService hostTopologyService){
        XMessaging.createListener("stream.system.cpu.usage",
                new SystemCpuUsageStreamListener(hostTopologyService));
    }
}
