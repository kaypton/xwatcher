package com.github.fenrir.xapiserver.services.resources.v1.impl;

import com.alibaba.fastjson.JSON;
import com.github.fenrir.xapiserver.XApiServerApplication;
import com.github.fenrir.xapiserver.components.etcd.EtcdClient;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdDeleteResponse;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdGetResponse;
import com.github.fenrir.xapiserver.configs.ApplicationConfig;
import com.github.fenrir.xapiserver.resources.v1.XLocalMonitor;
import com.github.fenrir.xapiserver.services.resources.v1.XLocalMonitorService;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorDeleteResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorGetResponse;
import com.github.fenrir.xcommon.clients.xapiserver.api.rest.responseEntities.api.v1.XLocalMonitorUpdateResponse;
import com.github.fenrir.xcommon.utils.CommonUtils;
import org.springframework.stereotype.Service;

@Service("XLocalMonitorService")
public class XLocalMonitorServiceImpl implements XLocalMonitorService {

    private EtcdClient etcdClient = null;
    private final String etcdPrefix;

    public XLocalMonitorServiceImpl(){
        this.etcdPrefix = ApplicationConfig.etcdPrefix;
    }

    private EtcdClient getEtcdClient(){
        if(this.etcdClient == null){
            this.etcdClient = (EtcdClient) XApiServerApplication.context.getBean("EtcdClient");
        }
        return this.etcdClient;
    }

    @Override
    public XLocalMonitorUpdateResponse update(String hostname, String ipAddress){
        XLocalMonitorUpdateResponse response = new XLocalMonitorUpdateResponse();
        boolean valid = CommonUtils.ipAddressIsValid(ipAddress);
        if(!valid){
            response.setStatus(1);
            response.setMsg("ip address is invalid");
            return response;
        }

        response.setStatus(0);
        XLocalMonitor xLocalMonitor = this.getXLocalMonitor(hostname, ipAddress);
        if(xLocalMonitor == null){
            xLocalMonitor = new XLocalMonitor(hostname, ipAddress);
            this.putXLocalMonitor(xLocalMonitor);
            response.setMsg("Register Success");
        }else{
            this.setXLocalMonitorLastUpdateUnixTimestamp(hostname, ipAddress);
            response.setMsg("Update Success");
        }

        response.setRpcServerTopic(xLocalMonitor.getRpcServerTopic());

        return response;
    }

    @Override
    public XLocalMonitorGetResponse get(String hostname, String ipAddress){
        XLocalMonitorGetResponse response = new XLocalMonitorGetResponse();
        XLocalMonitor ret = this.getXLocalMonitor(hostname, ipAddress);
        if(ret == null){
            response.setStatus(1);
            response.setMsg("No such XLocalMonitor");
        }else{
            response.setStatus(0);
            response.setMsg("Get Success");
            XLocalMonitorGetResponse.XLocalMonitorRecord record = new XLocalMonitorGetResponse.XLocalMonitorRecord();
            record.setHostname(ret.getHostname());
            record.setRpcServerTopic(ret.getRpcServerTopic());
            record.setId(ret.getId());
            record.setIpAddress(ret.getIpAddress());
            Long lastUpdateUnixTimestamp = this.getXLocalMonitorLastUpdateUnixTimestamp(hostname, ipAddress);
            if(lastUpdateUnixTimestamp != null)
                record.setLastUpdateUnixTimestamp(lastUpdateUnixTimestamp);
            response.addXLocalMonitorRecord(record);
        }
        return response;
    }

    @Override
    public XLocalMonitorGetResponse getAll(){
        XLocalMonitorGetResponse response = new XLocalMonitorGetResponse();
        String prefix = this.etcdPrefix + XLocalMonitor.prefix;
        EtcdGetResponse _response = this.getEtcdClient().getPrefix(prefix);
        if(_response.getStatus() != 0){
            response.setStatus(_response.getStatus());
            response.setMsg("[ETCD EXCEPTION] " + _response.getMsg());
        }else{
            response.setStatus(0);
            response.setMsg("Get Success");
            for(int i = 0; i < _response.getSize(); i++){
                if(_response.getKey(i).endsWith("lastUpdateUnixTimestamp")){
                    continue;
                }
                XLocalMonitorGetResponse.XLocalMonitorRecord record = JSON.parseObject(_response.getValue(i), XLocalMonitorGetResponse.XLocalMonitorRecord.class);
                Long ts = this.getXLocalMonitorLastUpdateUnixTimestamp(record.getHostname(), record.getIpAddress());
                if(ts != null)
                    record.setLastUpdateUnixTimestamp(ts);
                response.addXLocalMonitorRecord(record);
            }
        }
        return response;
    }

    @Override
    public XLocalMonitorDeleteResponse delete(String hostname, String ipAddress) {
        XLocalMonitorDeleteResponse response = new XLocalMonitorDeleteResponse();
        EtcdDeleteResponse _response = this.deleteXLocalMonitor(hostname, ipAddress);
        if(_response != null){
            response.setStatus(_response.getStatus());
            response.setMsg("[ETCD EXCEPTION] " + _response.getMsg());
        }else{
            response.setStatus(0);
            response.setMsg("Delete Success");
        }
        return response;
    }

    private EtcdDeleteResponse deleteXLocalMonitor(String hostname, String ipAddress){
        String path = this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(hostname, ipAddress);
        EtcdDeleteResponse deleteResponse = this.getEtcdClient().delete(path);
        if(deleteResponse.getStatus() != 0){
            return deleteResponse;
        }
        path = this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(hostname, ipAddress) + "/lastUpdateUnixTimestamp";
        deleteResponse = this.getEtcdClient().delete(path);
        if(deleteResponse.getStatus() != 0){
            return deleteResponse;
        }
        return null;
    }

    private XLocalMonitor getXLocalMonitor(String hostname, String ipAddress){
        EtcdGetResponse getResponse =
                this.getEtcdClient().get(this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(hostname, ipAddress));
        if(getResponse.getSize() == 0){
            return null;
        }
        return JSON.parseObject(getResponse.getValue(0), XLocalMonitor.class);
    }

    private Long getXLocalMonitorLastUpdateUnixTimestamp(String hostname, String ipAddress){
        String path = this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(hostname, ipAddress) + "/lastUpdateUnixTimestamp";
        EtcdGetResponse response = this.getEtcdClient().get(path);
        if(response.getSize() == 0){
            return null;
        }else{
            return Long.valueOf(response.getValue(0));
        }
    }

    private void setXLocalMonitorLastUpdateUnixTimestamp(String hostname, String ipAddress){
        String path = this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(hostname, ipAddress) + "/lastUpdateUnixTimestamp";
        this.getEtcdClient().put(path, String.valueOf(CommonUtils.getTimestamp()));
    }

    private void putXLocalMonitor(XLocalMonitor xLocalMonitor){
        String path = this.etcdPrefix + XLocalMonitor.prefix + this.getXLocalMonitorLabel(xLocalMonitor.getHostname(), xLocalMonitor.getIpAddress());
        this.getEtcdClient().put(path, JSON.toJSONString(xLocalMonitor));
        this.setXLocalMonitorLastUpdateUnixTimestamp(xLocalMonitor.getHostname(), xLocalMonitor.getIpAddress());
    }

    public String getXLocalMonitorLabel(String hostname, String ipAddress){
        StringBuilder sb = new StringBuilder();
        sb.append(hostname).append("-");
        ipAddress = ipAddress.replaceAll("\\.", "-");
        sb.append(ipAddress);
        return sb.toString();
    }


}
