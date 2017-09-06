package com.puyixiaowo.medis.main;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.puyixiaowo.medis.Routes;
import com.puyixiaowo.medis.bean.sys.AppConfigBean;
import com.puyixiaowo.medis.error.ErrorHandler;
import com.puyixiaowo.medis.generator.utils.CustomIdSerializer;
import com.puyixiaowo.medis.utils.AppUtils;
import com.puyixiaowo.medis.utils.ConfigUtils;

import static spark.Spark.port;

/**
 * @author Moses
 * @date 2017-08-01 18:21
 */
public class Main {
    /**
     * 支持启动设置端口：java -jar f-blog-1.0.jar -p 1521,
     * 默认启动端口8003
     * @param args
     */
    public static void main(String[] args) {

        AppConfigBean config = AppUtils.getAppConfigBean(args);
        port(config.getPort());

        ConfigUtils.init();

        ErrorHandler.handleErrors();
        Routes.init();

        //ID序列化为字符串类型
        SerializeConfig.getGlobalInstance().put(Long.class, new CustomIdSerializer());
    }
}
