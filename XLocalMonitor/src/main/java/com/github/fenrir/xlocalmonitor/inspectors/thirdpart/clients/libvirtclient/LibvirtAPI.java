package com.github.fenrir.xlocalmonitor.inspectors.thirdpart.clients.libvirtclient;

import com.github.fenrir.xlocalmonitor.annotations.Inspector;
import lombok.Getter;
import lombok.Setter;
import org.libvirt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Inspector(name = "libvirt")
public class LibvirtAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger("LibvirtAPI");

    private Connect connect = null;
    private static String libvirtConnectionURL = null;

    public LibvirtAPI(){}

    public Connect getConnect() {
        return connect;
    }

    public void setConnect(Connect connect) {
        this.connect = connect;
    }

    public static String getLibvirtConnectionURL() {
        return libvirtConnectionURL;
    }

    public static void setLibvirtConnectionURL(String libvirtConnectionURL) {
        LibvirtAPI.libvirtConnectionURL = libvirtConnectionURL;
    }

    public void connect(){
        LOGGER.info("LibvirtAPI prepare to connect to libvirt");

        try {
            this.setConnect(new Connect(getLibvirtConnectionURL(), true));
        } catch (LibvirtException e) {
            e.printStackTrace();
        }

        try {
            LOGGER.info("connect to host : " + getConnect().getHostName());
            LOGGER.info("libvirt version : " + getConnect().getLibVersion());
            LOGGER.info("connected URL   : " + getConnect().getURI());
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
    }

    public int[] getAllActiveDomainIds(){
        try{
            return getConnect().listDomains();
        } catch (LibvirtException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get cpu info from a specific domain<br><br>
     * @param domainId domain id
     * @return a list of DomainVCpuInfo
     */
    public List<DomainVCpuInfo> getCpuInfoByDomainId(int domainId){
        List<DomainVCpuInfo> domainVCpuInfoList = new ArrayList<>();
        try{
            VcpuInfo[] vcpusInfo = this._getDomainById(domainId).getVcpusInfo();
            for(VcpuInfo vcpuInfo : vcpusInfo){
                domainVCpuInfoList.add(new DomainVCpuInfo(vcpuInfo));
            }
            return domainVCpuInfoList;
        } catch (LibvirtException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get cpu time from a specific domain<br>
     * if something wrong. return -1<br><br>
     * @param domainId domain id
     * @return cpu time in nanoseconds
     */
    public long getCpuTimeByDomainId(int domainId){
        try{
            return this._getDomainById(domainId).getInfo().cpuTime;
        } catch (LibvirtException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * get max memory size allowed from a specific domain<br>
     * if something wrong. return -1<br><br>
     * @param domainId domain id
     * @return domain max memory size allowed in KBytes
     */
    public long getMaxMemoryByDomainId(int domainId){
        try{
            return this._getDomainById(domainId).getInfo().maxMem;
        } catch (LibvirtException e){
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * get memory used from a specific domain<br>
     * if something wrong. return -1<br><br>
     * @param domainId domain id
     * @return memory used by the domain
     */
    public long getMemoryUsedByDomainId(int domainId){
        try{
            return this._getDomainById(domainId).getInfo().memory;
        } catch (LibvirtException e){
            e.printStackTrace();
            return -1;
        }
    }

    public Domain getDomainById(int domainId){
        try{
            return this._getDomainById(domainId);
        } catch (LibvirtException e){
            e.printStackTrace();
            return null;
        }
    }

    private Domain _getDomainById(int domainId) throws LibvirtException {
        return getConnect().domainLookupByID(domainId);
    }
}
