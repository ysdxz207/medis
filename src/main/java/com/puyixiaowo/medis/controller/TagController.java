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

public class TagController {

    /**
     * 获取标签列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String tags(Request request, Response response) {

        return ConfigUtils.readTags().toJSONString();
    }

    /**
     *
     * @param request
     * @param response
     * @return
     */
    public static Object tag(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();

        //读取配置
        model.put("confList", ConfigUtils.readRedisConf());

        return new FreeMarkerTemplateEngine()
                .render(new ModelAndView(model,
                        "tag.html"));
    }


    /**
     * 添加标签
     *
     * @param request
     * @param response
     * @return
     */
    public static String add(Request request, Response response) {


        JSONArray tags = ConfigUtils.readTags();;
        JSONObject tag = new JSONObject();
        tag.put("name", request.queryParams("name"));
        tag.put("value", request.queryParams("value"));
        if (!tags.contains(tag)) {
            tags.add(tag);
        }

        ConfigUtils.saveTags(tags.toJSONString());
        return "{}";
    }


    public static Object delete(Request request, Response response) {

            String name = request.queryParams("name");
            String value = request.queryParams("value");

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("value", value);


            JSONArray tags = ConfigUtils.readTags();
            boolean flag = tags.remove(jsonObject);

            try {
                ConfigUtils.saveTags(tags.toJSONString());
                if (flag) {
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
    }
}
