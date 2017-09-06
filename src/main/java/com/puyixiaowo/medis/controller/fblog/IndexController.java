package com.puyixiaowo.medis.controller.fblog;

import spark.Request;
import spark.Response;

public class IndexController {

    /**
     * 首页
     * @param request
     * @param response
     * @return
     */
    public static Object index(Request request, Response response){
        return "I am index.";
    }
}
