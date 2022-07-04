package com.mirth.connect.util;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.Channel;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.DefaultConfigurationController;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.userutil.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * 注册中心
 */
public class NacosUtil {

    private static NamingService namingService = null;
    private static ConfigService configService = null;

    private static final ChannelController channelController = ControllerFactory.getFactory().createChannelController();
    private static final EngineController engineController = ControllerFactory.getFactory().createEngineController();

    static boolean isRunning = false;

    private static HashMap<String, Listener> listenerHashMap = new HashMap<>();

    public static Properties getProperties() {
        try {
            return DefaultConfigurationController.getInstance().getNacosProperties();
        } catch (ControllerException e) {
            return null;
        }
    }

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
                nacosProperties = getProperties();

                if (!nacosProperties.containsKey("server.addr")) {
                    return null;
                }
                namingService = NacosFactory.createNamingService(nacosProperties.getProperty("server.addr"));
            } catch (NacosException e) {
                e.printStackTrace();
            }
            return namingService;
        }
    }

    public static ConfigService getConfigService() {
        if (configService != null) {
            return configService;
        }
        synchronized (NacosUtil.class) {
            if (configService != null) {
                return configService;
            }
            Properties nacosProperties = null;
            try {
                nacosProperties = getProperties();

                if (!nacosProperties.containsKey("server.addr")) {
                    return null;
                }
                configService = NacosFactory.createConfigService(nacosProperties.getProperty("server.addr"));
            } catch (NacosException e) {
                e.printStackTrace();
            }
            return configService;
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
            if (StringUtils.isEmpty(addr) ||
                    "127.0.0.1".equals(addr) ||
                    "0.0.0.0".equals(addr) ||
                    "localhost".equals(addr)) {
                addr = IpUtil.getLocalIp();
            }
            nameService.deregisterInstance(channelName.replace("_", "-"), addr, port);
            return true;
        } catch (NacosException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean addChannelDeployListener() {
        ConfigService configService = getConfigService();
        if (configService == null) {
            return false;
        }
        try {
            Listener listener = new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String s) {
                    if (StringUtils.isEmpty(s) || !s.contains(":")) {
                        return;
                    }
                    String channelId = s.substring(0, s.lastIndexOf(":"));
                    Channel channel = channelController.getDeployedChannelById(channelId);
                    Channel lastChannel = channelController.getChannelById(channelId);
                    if (lastChannel == null) {
                        return;
                    }
                    if (channel != null) {
                        if (channel.getRevision() != lastChannel.getRevision()) {
                            engineController.deployChannels(Collections.singleton(channelId), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
                        }
                    } else {
                        engineController.deployChannels(Collections.singleton(channelId), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, null);
                    }
                }
            };
            configService.addListener("channel.deploy", "mirth.config", listener);

            listenerHashMap.put("DeployListener", listener);
        } catch (NacosException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean removeChannelDeployListener() {
        ConfigService configService = getConfigService();
        if (configService == null) {
            return false;
        }
        if (listenerHashMap.containsKey("DeployListener")) {
            configService.removeListener("channel.deploy", "mirth.config", listenerHashMap.get("DeployListener"));
            listenerHashMap.remove("DeployListener");
        }
        return true;
    }

    public static boolean publishConfig(String channelId) {
        if (!isIsRunning()) {
            return false;
        }

        ConfigService configService = getConfigService();
        if (configService == null) {
            return false;
        }
        try {
            configService.publishConfig("channel.deploy", "mirth.config", channelId + ":" + DateUtil.getCurrentDate("yyyyMMddHHmmssSSS"));
        } catch (NacosException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean isIsRunning() {
        return isRunning;
    }

    public static void setIsRunning(boolean isRunning) {
        NacosUtil.isRunning = isRunning;
    }
}
