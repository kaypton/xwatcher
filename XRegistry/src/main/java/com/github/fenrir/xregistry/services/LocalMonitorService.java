package com.github.fenrir.xregistry.services;

import com.github.fenrir.xcommon.clients.xregistry.entities.mongo.LocalMonitor;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.BaseResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.RegisterResponseMessageBuilder;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.LocalMonitorInfoResponseMessage;
import com.github.fenrir.xcommon.clients.xregistry.types.rest.constants.RegisterStatus;
import com.github.fenrir.xcommon.utils.xregistry.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LocalMonitorService {
    private final LocalMonitorMongoRepo localMonitorMongoRepo;

    public LocalMonitorService(@Autowired LocalMonitorMongoRepo localMonitorMongoRepo){
        this.localMonitorMongoRepo = localMonitorMongoRepo;
    }

    public RegisterResponseMessage register(RegisterMessage registerMessage){
        // (hostname, type, ipaddr) => SHA-256
        String localMonitorID = this.getLocalMonitorIdFromRegisterMessage(registerMessage);
        String rpcServerTopic = "localmonitor." + localMonitorID;

        if(this.searchLocalMonitorById(localMonitorID).isPresent()){
            return RegisterResponseMessageBuilder.builder()
                    .setStatus(RegisterStatus.EXIST)
                    .setMessage("localmonitor already exists")
                    .setRpcServerTopic(rpcServerTopic)
                    .build();
        }else{
            LocalMonitor localMonitor = new LocalMonitor();
            localMonitor.setLocalMonitorId(localMonitorID);
            localMonitor.setLocalMonitorType(registerMessage.getLocalMonitorType().toString());
            localMonitor.setHostname(registerMessage.getHostname());
            localMonitor.setIp(registerMessage.getIpAddr());
            localMonitor.setRpcServerTopic(rpcServerTopic);
            this.addLocalMonitor(localMonitor);

            return RegisterResponseMessageBuilder.builder()
                    .setStatus(RegisterStatus.OK)
                    .setMessage("register done")
                    .setRpcServerTopic(rpcServerTopic)
                    .build();
        }
    }

    public LocalMonitorInfoResponseMessage getAll(){
        List<LocalMonitor> localMonitorList = this.localMonitorMongoRepo.findAll();
        if(localMonitorList.size() == 0) return new LocalMonitorInfoResponseMessage(1, "None", null);
        else return new LocalMonitorInfoResponseMessage(0, "Success", localMonitorList);
    }

    public LocalMonitorInfoResponseMessage get(String id){
        Optional<LocalMonitor> localMonitor = this.localMonitorMongoRepo.findById(id);
        List<LocalMonitor> localMonitorList = new ArrayList<>();
        if(localMonitor.isPresent()){
            localMonitorList.add(localMonitor.get());
            return new LocalMonitorInfoResponseMessage(0, "Success", localMonitorList);
        }else return new LocalMonitorInfoResponseMessage(1, "None", null);
    }

    public BaseResponseMessage deleteById(String id){
        Optional<LocalMonitor> localMonitor = this.searchLocalMonitorById(id);
        if(localMonitor.isPresent()) {
            this.localMonitorMongoRepo.deleteById(id);
            return new BaseResponseMessage(0, "Delete Success!");
        }else{
            return new BaseResponseMessage(1, "Delete failed! ID does not exists");
        }
    }

    private void addLocalMonitor(LocalMonitor localMonitor){
        this.localMonitorMongoRepo.save(localMonitor);
    }

    private Optional<LocalMonitor> searchLocalMonitorById(String id){
        return this.localMonitorMongoRepo.findById(id);
    }

    private String getLocalMonitorIdFromRegisterMessage(RegisterMessage registerMessage){
        return HashUtil.getLocalMonitorSHA256Hash(
                registerMessage.getHostname(),
                registerMessage.getLocalMonitorType(),
                registerMessage.getIpAddr()
        );
    }
}
