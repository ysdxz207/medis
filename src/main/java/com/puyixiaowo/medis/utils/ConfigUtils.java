package com.puyixiaowo.medis.utils;

/**
 * @author feihong
 * @date 2017-08-12 23:05
 */
public class ConfigUtils {

    /**
     * 初始化配置有顺序
     */
    public static void init() {
        initRedis();
    }

    /**
     * 初始化redis配置
     */
    private static void initRedis() {
        RedisUtils.testConnection();
    }
}
