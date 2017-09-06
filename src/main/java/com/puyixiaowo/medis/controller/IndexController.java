package com.puyixiaowo.medis.controller;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.bean.RedisCountBean;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.utils.FileUtils;
import com.puyixiaowo.medis.utils.RedisUtils;
import com.puyixiaowo.medis.utils.ResourceUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
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

        model.put("countList", RedisUtils.count().toJavaList(RedisCountBean.class));
//        model.put("dbList", RedisUtils.dbList().toJavaList(RedisCountBean.class));

        return new FreeMarkerTemplateEngine()
                .render(new ModelAndView(model,
                        "index.html"));
    }

    /**
     * 获取标签列表
     *
     * @param request
     * @param response
     * @return
     */
    public static String tags(Request request, Response response) {

        return FileUtils.readResourceFile("tag/tags.json");
    }


    /**
     * 根据key查找值
     *
     * @param request
     * @param response
     * @return
     */
    public static String redisGet(Request request, Response response) {
        return RedisUtils.get(request.queryParams("key"));
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
}
