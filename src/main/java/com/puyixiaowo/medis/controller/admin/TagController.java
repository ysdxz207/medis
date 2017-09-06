package com.puyixiaowo.medis.controller.admin;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.TagBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.TagService;
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
public class TagController extends BaseController {

    @RequiresPermissions(value = {"tag:view"})
    public static String tags(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/tag/tag_list.html"));
        }
        PageBean pageBean = getPageBean(request);
        try {
            TagBean tagBean = getParamsEntity(request, TagBean.class, false);
            List<TagBean> list = TagService.selectTagList(tagBean,
                    pageBean);
            pageBean.setList(list);

            int count = TagService.selectCount(tagBean);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }


    @RequiresPermissions(value = {"tag:edit"})
    public static String edit(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();
        List<TagBean> tagBeanList = getParamsEntityJson(request, TagBean.class, true);
        try {

            for (TagBean tagBean :
                    tagBeanList) {
                DBUtils.insertOrUpdate(tagBean);
            }
        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"tag:delete"})
    public static String delete(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();

        try {
            DBUtils.deleteByIds(TagBean.class,
                    request.queryParams("id"));
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"tag:view"})
    public static String topArray(Request request) {
        List<String> list = DBUtils.selectList(String.class,
                "select name from tag ",
                null);

        return JSON.toJSONString(list);
    }

}
