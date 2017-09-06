package com.puyixiaowo.medis.controller;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.bean.RedisCountBean;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.utils.RedisUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndexController {

    /**
     * 首页
     * @param request
     * @param response
     * @return
     */
    public static Object index(Request request, Response response){

        Map<String, Object> model = new HashMap<>();

        model.put("countList", RedisUtils.count().toJavaList(RedisCountBean.class));

        return new FreeMarkerTemplateEngine()
                .render(new ModelAndView(model,
                        "index.html"));
    }
}
