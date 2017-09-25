package com.puyixiaowo.medis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puyixiaowo.medis.bean.RedisCountBean;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.utils.ConfigUtils;
import com.puyixiaowo.medis.utils.RedisUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

public class IndexController {

    /**
     * 首页
     *
     * @param request
     * @param response
     * @return
     */
    public static Object index(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();

        //读取配置
        model.put("confList", ConfigUtils.readRedisConf());

        return new FreeMarkerTemplateEngine()
                .render(new ModelAndView(model,
                        "index.html"));
    }




    /**
     * 根据key查找值
     *
     * @param request
     * @param response
     * @return
     */
    public static String redisGet(Request request, Response response) {
        Integer db = Integer.valueOf(request.queryParams("db"));
        String key = request.queryParams("key");
        String result = "";
        try {
            result = RedisUtils.get(db, key);
        } catch (Exception e) {
            result = JSON.toJSONString(RedisUtils.hvals(db, key));
        }
        return result;
    }

    /**
     * 根据标签获取key列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String redisKeys(Request request, Response response) {

        return JSON.toJSONString(RedisUtils
                .keys(Integer.parseInt(request.queryParams("db")),
                        request.queryParams("key")));
    }

    public static Object redisDelete(Request request, Response response) {
        return RedisUtils.delete(Integer.valueOf(request.queryParams("db")),
                request.queryParams("key"));
    }

    public static Object editDelete(Request request, Response response) {
        boolean success = false;
        try {
            RedisUtils.set(Integer.valueOf(request.queryParams("db")),
                    request.queryParams("key"),
                    request.queryParams("value"));
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    public static Object saveConfAndConnect(Request request, Response response) {
        //保存配置
        String host = request.queryParams("host");
        String port = request.queryParams("port");
        String pass = request.queryParams("pass");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("host", host);
        jsonObject.put("port", port);
        jsonObject.put("pass", pass);
        JSONArray conf = ConfigUtils.readRedisConf();

        if (!conf.contains(jsonObject)) {
            conf.add(jsonObject);
        }

        ConfigUtils.saveRedisConf(conf.toJSONString());

        RedisUtils.init(request.queryParams("host"),
                Integer.valueOf(request.queryParams("port")),
                request.queryParams("pass"));
        return RedisUtils.isConnected();
    }

    public static Object confDelete(Request request, Response response) {
        String host = request.queryParams("host");
        String port = request.queryParams("port");
        String pass = request.queryParams("pass");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("host", host);
        jsonObject.put("port", port);
        jsonObject.put("pass", pass);


        JSONArray conf = ConfigUtils.readRedisConf();
        boolean flag = conf.remove(jsonObject);

        try {
            ConfigUtils.saveRedisConf(conf.toJSONString());
            if (flag) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }

    }

    public static Object count(Request request, Response response) {
        if (RedisUtils.isConnected()) {

            return JSON.toJSONString(RedisUtils.count().toJavaList(RedisCountBean.class));
        }

        return "";
    }
}
