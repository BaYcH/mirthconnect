package com.mirth.connect.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.server.controllers.DefaultConfigurationController;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;

/**
 * 注册中心
 */
public class NacosUtil {

    private static NamingService namingService = null;

    public static NamingService getNameService() {
        if (namingService != null) {
            return namingService;
        }
        synchronized (NacosUtil.class) {
            if (namingService != null) {
                return namingService;
            }
            Properties nacosProperties = null;
            try {
                nacosProperties = DefaultConfigurationController.getInstance().getNacosProperties();

                if (!nacosProperties.containsKey("server.addr")) {
                    return null;
                }
                namingService = NamingFactory.createNamingService(nacosProperties.getProperty("server.addr"));
            } catch (ControllerException e) {
                e.printStackTrace();
            } catch (NacosException e) {
                e.printStackTrace();
            }
            return namingService;
        }
    }

    /**
     * 注册HTTP服务至nacos
     *
     * @param addr        本机IP地址或域名
     * @param port        服务端口
     * @param channelName 服务名称/信道名
     * @return 注销成功/失败
     */
    public static boolean registerHttpServer(String addr, int port, String channelName) {
        NamingService nameService = getNameService();
        if (nameService == null) {
            return false;
        }
        try {
            if (StringUtils.isEmpty(addr) ||
                    "127.0.0.1".equals(addr) ||
                    "0.0.0.0".equals(addr) ||
                    "localhost".equals(addr)) {
                addr = IpUtil.getLocalIp();
            }
            nameService.registerInstance(channelName.replace("_", "-"), addr, port);
            return true;
        } catch (NacosException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注销服务
     *
     * @param addr        本机IP或域名
     * @param port        端口
     * @param channelName 信道名或者服务名称
     * @return 注销成功/失败
     */
    public static boolean deRegisterHttpServer(String addr, int port, String channelName) {
        NamingService nameService = getNameService();
        if (nameService == null) {
            return false;
        }
        try {
            nameService.deregisterInstance(channelName.replace("_", "-"), addr, port);
            return true;
        } catch (NacosException e) {
            e.printStackTrace();
            return false;
        }
    }


}
