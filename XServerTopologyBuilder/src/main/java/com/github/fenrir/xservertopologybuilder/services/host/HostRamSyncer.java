package com.github.fenrir.xservertopologybuilder.services.host;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.fenrir.xmessaging.XMessage;
import com.github.fenrir.xmessaging.MessageProcessCallBack;
import com.github.fenrir.xmessaging.XMessaging;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostRamSyncer {

    private static class StreamListener implements MessageProcessCallBack {
        @Getter @Setter private HostTopologyService service;

        public StreamListener(HostTopologyService service){
            this.setService(service);
        }
        @Override
        public void processMessage(XMessage data) {
            JSONObject _data = JSON.parseObject(data.getStringData());
            service.report(
                    _data.getJSONObject("hostInfo").getString("host"),
                    "mem.free.KiB",
                    _data.getJSONObject("value").getLongValue("mem.free.KiB")
            );

            service.report(
                    _data.getJSONObject("hostInfo").getString("host"),
                    "mem.total.KiB",
                    _data.getJSONObject("value").getLongValue("mem.total.KiB")
            );
        }
    }

    public HostRamSyncer(@Autowired HostTopologyService hostTopologyService){
        XMessaging.createListener("stream.system.ram", new StreamListener(hostTopologyService));
    }
}
