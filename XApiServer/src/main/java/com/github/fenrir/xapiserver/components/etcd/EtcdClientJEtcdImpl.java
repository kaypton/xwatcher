package com.github.fenrir.xapiserver.components.etcd;

import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdDeleteResponse;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdGetResponse;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdPutResponse;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.ClientBuilder;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.cluster.Member;
import io.etcd.jetcd.cluster.MemberListResponse;
import io.etcd.jetcd.common.exception.EtcdException;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EtcdClientJEtcdImpl implements EtcdClient{
    static private final Logger LOGGER = LoggerFactory.getLogger(EtcdClientJEtcdImpl.class);

    private Client client;

    public EtcdClientJEtcdImpl(String caFile,
                               String keyFile,
                               String certFile,
                               String password,
                               String user,
                               String authType,
                               String... endpoints){

        LOGGER.info("endpoints:");
        for(String endpoint: endpoints){
            LOGGER.info("   {}", endpoint);
        }

        ClientBuilder builder = Client.builder();
        try{
            builder = builder.endpoints(endpoints);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            System.exit(-1);
        }

        if(authType.equals("ssl")){
            SslContext sslContext;
            try {
                ApplicationProtocolConfig alpn = new ApplicationProtocolConfig(
                        ApplicationProtocolConfig.Protocol.ALPN,
                        ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                        ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT,
                        ApplicationProtocolNames.HTTP_2
                );

                sslContext = GrpcSslContexts.forClient()
                        .applicationProtocolConfig(alpn)
                        .sslProvider(SslProvider.JDK)
                        .trustManager(new File(caFile))
                        .keyManager(new File(certFile), new File(keyFile))
                        .build();

                LOGGER.info("SSL is client: {}", sslContext.isClient());
                LOGGER.info("SSL is server: {}", sslContext.isServer());
                builder.sslContext(sslContext);
            } catch (SSLException e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
                System.exit(-1);
            }
        }else if(authType.equals("password")){
            builder.password(ByteSequence.from(password.getBytes(StandardCharsets.UTF_8)));
            builder.user(ByteSequence.from(user.getBytes(StandardCharsets.UTF_8)));
        }


        try{
            this.client = builder.build();
            LOGGER.info("etcd client build successfully");
            try {
                MemberListResponse response = this.client.getClusterClient().listMember().get();
                List<Member> memberList = response.getMembers();
                for(Member member : memberList){
                    LOGGER.info("   member: name:{}, id:{}", member.getName(), member.getId());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        } catch (EtcdException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    public EtcdPutResponse put(String key, String value){
        EtcdPutResponse response = new EtcdPutResponse();

        ByteSequence keyByte = ByteSequence.from(key.getBytes(StandardCharsets.UTF_8));
        ByteSequence valueByte = ByteSequence.from(value.getBytes(StandardCharsets.UTF_8));

        try {
            PutResponse _response = this.client.getKVClient().put(keyByte, valueByte).get();
            response.setStatus(0);
            response.addPreKv(_response.getPrevKv().getKey().toString(StandardCharsets.UTF_8),
                    _response.getPrevKv().getValue().toString(StandardCharsets.UTF_8));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            response.setStatus(1);
            response.setMsg("[jetcd] " + e.getMessage());
        }
        return response;
    }

    @Override
    public EtcdGetResponse get(String key){
        EtcdGetResponse response = new EtcdGetResponse();

        ByteSequence keyByte = ByteSequence.from(key.getBytes(StandardCharsets.UTF_8));
        GetOption option = GetOption.newBuilder().isPrefix(false).build();
        try {
            GetResponse _response = this.client.getKVClient().get(keyByte, option).get();
            if(_response.getCount() != 1){
                if(_response.getCount() == 0){
                    response.setStatus(0);
                }else{
                    LOGGER.error("keys num is not 1");
                    response.setStatus(1);
                }
            }else{
                response.setStatus(0);
                response.addKv(
                        _response.getKvs().get(0).getKey().toString(StandardCharsets.UTF_8),
                        _response.getKvs().get(0).getValue().toString(StandardCharsets.UTF_8)
                );
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            response.setStatus(1);
            response.setMsg("[jetcd] " + e.getMessage());
        }
        return response;
    }

    @Override
    public EtcdGetResponse getPrefix(String prefix){
        EtcdGetResponse response = new EtcdGetResponse();
        GetOption option = GetOption.newBuilder()
                .isPrefix(true)
                .build();
        try{
            GetResponse _response = this.client.getKVClient().get(ByteSequence.from(prefix.getBytes(StandardCharsets.UTF_8)), option).get();
            response.setStatus(0);
            response.setMsg("Get Success");
            for(KeyValue kv : _response.getKvs()){
                response.addKv(kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue().toString(StandardCharsets.UTF_8));
            }
        } catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            response.setStatus(1);
            response.setMsg("[jetcd] " + e.getMessage());
        }
        return response;
    }

    @Override
    public EtcdDeleteResponse delete(String key){
        EtcdDeleteResponse response = new EtcdDeleteResponse();
        DeleteOption option = DeleteOption.newBuilder()
                .isPrefix(false).build();
        try {
            DeleteResponse _response = this.client.getKVClient().delete(ByteSequence.from(key.getBytes(StandardCharsets.UTF_8)), option).get();
            response.setStatus(0);
            for(KeyValue kv : _response.getPrevKvs()){
                response.addPreKv(kv.getKey().toString(StandardCharsets.UTF_8), kv.getValue().toString(StandardCharsets.UTF_8));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
            response.setStatus(1);
            response.setMsg("[jetcd] " + e.getMessage());
        }
        return response;
    }
}
