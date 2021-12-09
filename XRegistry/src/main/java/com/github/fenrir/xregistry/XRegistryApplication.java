package com.github.fenrir.xregistry;

import com.github.fenrir.xmessaging.rpc.annotation.RpcServerScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@RpcServerScan(path = {"com.github.fenrir.xregistry.rpc"})
@SpringBootApplication
public class XRegistryApplication {
    static public void main(String[] args){
        SpringApplication.run(XRegistryApplication.class);
    }
}
