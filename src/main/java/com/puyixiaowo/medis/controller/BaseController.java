package com.puyixiaowo.medis.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puyixiaowo.core.entity.Validatable;
import com.puyixiaowo.core.exceptions.ValidationException;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.constants.Constants;
import com.puyixiaowo.medis.exception.BaseControllerException;
import com.puyixiaowo.medis.utils.ResourceUtils;
import com.puyixiaowo.medis.utils.StringUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import spark.Request;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class BaseController {

    /**
     * @param request
     * @param clazz
     * @param validate 是否校验参数
     * @param <T>
     * @return
     */
    public static <T extends Validatable> T getParamsEntity(Request request,
                                                            Class<T> clazz,
                                                            boolean validate) throws ValidationException {
        Map<String, String[]> map = request.queryMap().toMap();
        T obj = null;
        try {
            obj = (T) Class.forName(clazz.getName()).newInstance();
            BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
            BeanUtils.populate(obj, map);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        if (validate) {
            obj.validate();
        }
        return obj;
    }

    public static <T> List<T> getParamsEntityJson(Request request,
                                                  Class<T> clazz,
                                                  boolean validate) {
        String json = request.queryParams("json");
        if (StringUtils.isBlank(json)) {
            throw new BaseControllerException("接收到的参数中不包含'json'");
        }

        JSONArray jsonArray = JSON.parseArray(json);

        return jsonArray.toJavaList(clazz);
    }

    public static JSONObject getParamsJSON(Request request,
                                           Class clazz) {
        Object t = getParamsEntity(request, clazz, false);
        return JSON.parseObject(JSON.toJSONString(t));
    }

    public static String renderHTML(String htmlFile) {
        try {
            // If you are using maven then your files
            // will be in a folder called resources.
            // getResource() gets that folder
            // and any files you specify.
            URL url = ResourceUtils.getResource(htmlFile);

            // Return a String which has all
            // the contents of the file.
            Path path = Paths.get(url.toURI());
            return new String(Files.readAllBytes(path), Charset.defaultCharset());
        } catch (IOException | URISyntaxException e) {
            // Add your own exception handlers here.
        }
        return null;
    }

    /*
     * 获取分页RowBouds
	 */
    public static PageBean getPageBean(Request request) {

        String pageCurrentStr = request.queryParams("pageCurrent");
        String pageSizeStr = request.queryParams("pageSize");
        int pageCurrent = 1;
        int pageSize = Constants.DEFAULT_PAGE_SIZE;
        if (StringUtils.isNotBlank(pageCurrentStr)) {
            pageCurrent = Integer.valueOf(pageCurrentStr);
        }
        if (StringUtils.isNotBlank(pageSizeStr)) {
            pageSize = Integer.valueOf(pageSizeStr);
        }
        return new PageBean(pageCurrent, pageSize);
    }
}
