package com.github.fenrir.xapiserver.components.etcd;

import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdDeleteResponse;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdGetResponse;
import com.github.fenrir.xapiserver.components.etcd.responseEntities.EtcdPutResponse;

public interface EtcdClient {
    EtcdPutResponse put(String key, String value);
    EtcdGetResponse get(String key);
    EtcdGetResponse getPrefix(String prefix);
    EtcdDeleteResponse delete(String key);
}
