package com.puyixiaowo.medis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puyixiaowo.medis.bean.RedisCountBean;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.utils.ConfigUtils;
import com.puyixiaowo.medis.utils.RedisUtils;
import com.puyixiaowo.medis.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.*;

public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

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
        JSONObject json = new JSONObject();
        Integer db = Integer.valueOf(request.queryParams("db"));
        String key = request.queryParams("key");
        String hkey = request.queryParams("hkey");
        String result = "";
        try {
            result = RedisUtils.get(db, key);
        } catch (Exception e) {
            if (StringUtils.isBlank(hkey)) {
                List<JSONObject> jsonList = new ArrayList<>();
                List<String> stringList = RedisUtils.hvals(db, key);
                json.put("count", stringList.size());
                if (stringList.size() > 200) {
                    stringList = stringList.subList(0, 200);
                }
                for (String jsonStr : stringList) {
                    jsonList.add(JSON.parseObject(jsonStr));
                }
                result = JSON.toJSONString(jsonList);
            } else {
                result = RedisUtils.hget(db, key, hkey);
            }
        }

        json.put("result", result == null ? "" : result);
        return json.toJSONString();
    }

    /**
     * 根据标签获取key列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String redisKeys(Request request, Response response) {
        JSONObject json = new JSONObject();
        Set<String> keys = RedisUtils
                .keys(Integer.parseInt(request.queryParams("db")),
                        request.queryParams("key"));

        List<String> list = new ArrayList(keys);
        json.put("count", keys.size());
        if (keys.size() > 200) {
            list = list.subList(0, 199);
        }
        json.put("result", list);
        return json.toJSONString();
    }

    public static Object redisDelete(Request request, Response response) {

        JSONObject json = new JSONObject();
        json.put("status", false);
        boolean success = true;
        Integer db = Integer.valueOf(request.queryParams("db"));
        String key = request.queryParams("key");
        String hkey = request.queryParams("hkey");
        /**
         * 0、key，1、hkey
         */
        int type = Integer.valueOf(request.queryParamOrDefault("type", "0"));

        if (type == 1
                && StringUtils.isBlank(hkey)) {
            json.put("msg", "hkey为空");
            return json;
        }
        try {
            if (StringUtils.isBlank(hkey)) {
                RedisUtils.delete(db, key);
            } else {
                RedisUtils.hdel(db, key, hkey);
            }
        } catch (Exception e) {
            success = false;
            json.put("msg", e.getMessage() == null ? JSON.toJSONString(e) : e.getMessage());
            logger.error("删除异常：" + e.getMessage());
        }

        json.put("status", success);
        return json;
    }

    public static Object redisEdit(Request request, Response response) {
        Integer db = Integer.valueOf(request.queryParams("db"));
        String key = request.queryParams("key");
        String hkey = request.queryParams("hkey");
        String value = request.queryParams("value");

        boolean success = true;
        try {
            if (StringUtils.isBlank(hkey)) {
                RedisUtils.set(db, key, value);
            } else {
                RedisUtils.hset(db, key, hkey, value);
            }
        } catch (Exception e) {
            success = false;
            logger.error("修改值异常：" + e.getMessage());
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
            logger.error("删除配置异常：" + e.getMessage());
            return false;
        }

    }

    public static Object count(Request request, Response response) {
        if (RedisUtils.isConnected()) {

            return JSON.toJSONString(RedisUtils.count().toJavaList(RedisCountBean.class));
        }

        return "";
    }

    public static Object exit(Request request, Response response) {
        if (RedisUtils.isConnected()) {
            RedisUtils.close();
        }
        System.exit(0);
        return "";
    }
}
