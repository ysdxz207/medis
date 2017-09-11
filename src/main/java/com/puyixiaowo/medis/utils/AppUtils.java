package com.puyixiaowo.medis.utils;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.bean.AppConfigBean;
import com.puyixiaowo.medis.enums.EnumAppConfig;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Moses
 * @date 2017-09-01 13:33
 */
public class AppUtils {

    private static Map<String, String> argMap = new LinkedHashMap<>();


    /**
     * 将启动命令参数组装为AppConfigBean
     * @param args
     * @return
     */
    public static AppConfigBean getAppConfigBean(String[] args) {

        Iterator<String> it = Arrays.asList(args).iterator();
        while (it.hasNext()) {
            String arg = it.next();
            EnumAppConfig enumAppConfig = EnumAppConfig.getEnum(arg);
            if (enumAppConfig != null) {
                argMap.put(enumAppConfig.field, it.next());
            }
        }

        return JSON.parseObject(JSON.toJSONString(argMap), AppConfigBean.class);
    }

    public static boolean isLinux() {
        return !System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
    }
}
