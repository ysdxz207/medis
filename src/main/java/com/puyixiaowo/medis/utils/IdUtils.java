package com.puyixiaowo.medis.utils;

/**
 * ID生成工具
 * @author feihong
 * @date 2017-08-10 22:09
 */
public class IdUtils {
   private static SnowflakeIdWorker idWorker;

    static {
        idWorker = new SnowflakeIdWorker(0, 0);
    }

    public static Long generateId(){

        return idWorker.nextId();
    }
}
