# XWatcher 

### 组件概览:

- XCommon
  - 包含共用功能或数据结构，例如：
    - Action数据结构定义
    - Event & Stream 数据结构定义
- XDashboard
  - XWatcher WebUI
- XFunnel
  - 即将弃用
- XLocalMonitor
  - 本地监控器，部署到物理服务器或者K8s的Master节点上
    - 部署在物理服务器上的Local Monitor用来收集服务器本地指标数据
    - 部署在K8s Master节点上的Local Monitor用来收集此K8s集群的各类指标数据
- XMessaging
  - 通信类库，包含数据流与RPC
- XOpenStackAgent
  - OpenStack代理，完成Action在OpenStack平台上的执行
- XPlanner
- XRegistry
  - 注册中心，目前负责Local Monitor的注册以及发现
- XServerTopologyBuilder
- XSniffer
- XStrategy

### 部署

#### XRegistry 部署
指定环境变量：
```
PORT            - example: PORT=8080. default: 8080
NATS_ADDRESS    - example: nats://127.0.0.1:4222.
MONGODB_ADDRESS - example: localhost:27017. default: localhost:27017
MONGODB_DB      - example: xregistry. default: xregistry
```

#### XLocalMonitor 部署
指定环境变量:
```
NATS_HOST          - example: nats://222.201.144.196:4222
PORT               - example: 8080. default: 8080
LIBVIRT_CONNECTION - example: qemu:///system. default: none
NETDATA_HOST       - example: 127.0.0.1:19999. default: none
XINSPECTOR_HOST    - example: http://127.0.0.1:20000. default: none
ENABLE_MONITORS    - example: NetdataCpuOverloadMonitor,NetdataMemOverloadMonitor
MY_HOSTNAME        - example: compute1
CONFIG_FILE        - example: /etc/xlocalmonitor/config.yml. default: none
XREGISTRY_ADDRESS  - example: 127.0.0.1:8080
LOCALMONITOR_TYPE  - example: PhysicalMachine
BIND_ADDRESS       - example: 222.201.144.196
```

#### XDashboard 部署
指定环境变量:
```
NATS_ADDRESS      - example: nats://127.0.0.1:4222
XREGISTRY_ADDRESS - example: 127.0.0.1:8080
```
