package com.github.fenrir.xmessaging.rpc.annotation;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcServerScan {
    String[] path();
}
