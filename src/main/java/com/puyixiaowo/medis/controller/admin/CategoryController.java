package com.puyixiaowo.medis.controller.admin;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.CategoryBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.CategoryService;
import com.puyixiaowo.medis.utils.DBUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * 
 * @author Moses
 * @date 2017-09-05 22:31:38
 * 
 */
public class CategoryController extends BaseController {

    @RequiresPermissions(value = {"category:view"})
    public static String categorys(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/category/category_list.html"));
        }
        PageBean pageBean = getPageBean(request);
        try {
            CategoryBean categoryBean = getParamsEntity(request, CategoryBean.class, false);
            List<CategoryBean> list = CategoryService.selectCategoryList(categoryBean,
                    pageBean);
            pageBean.setList(list);

            int count = CategoryService.selectCount(categoryBean);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }


    @RequiresPermissions(value = {"category:edit"})
    public static String edit(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();
        List<CategoryBean> categoryBeanList = getParamsEntityJson(request, CategoryBean.class, true);
        try {

            for (CategoryBean categoryBean :
                    categoryBeanList) {
                DBUtils.insertOrUpdate(categoryBean);
            }
        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"category:delete"})
    public static String delete(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();

        try {
            DBUtils.deleteByIds(CategoryBean.class,
                    request.queryParams("id"));
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }


    @RequiresPermissions(value = {"category:view"})
    public static String allArray(Request request) {
        List<CategoryBean> list = DBUtils.selectList(CategoryBean.class,
                "select * from category ",
                null);

        return JSON.toJSONString(list);
    }

}
