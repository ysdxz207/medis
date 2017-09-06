package com.puyixiaowo.medis.controller.admin;

import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.PermissionBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.PermissionService;
import com.puyixiaowo.medis.utils.DBUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * @author feihong
 * @date 2017-08-13 18:58
 */
public class PermissionController extends BaseController {

    @RequiresPermissions(value = {"permission:view"})
    public static String permissions(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/permission/permission_list.html"));
        }
        PageBean pageBean = getPageBean(request);
        try {
            PermissionBean permissionBean = getParamsEntity(request, PermissionBean.class, false);
            List<PermissionBean> list = PermissionService.selectPermissionList(permissionBean,
                    pageBean);
            pageBean.setList(list);

            int count = PermissionService.selectCount(permissionBean);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }


    @RequiresPermissions(value = {"permission:edit"})
    public static String edit(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();
        List<PermissionBean> permissionBeanList = getParamsEntityJson(request, PermissionBean.class, true);
        try {

            for (PermissionBean permissionBean :
                    permissionBeanList) {
                DBUtils.insertOrUpdate(permissionBean);
            }
        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"permission:delete"})
    public static String delete(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();

        try {
            DBUtils.deleteByIds(PermissionBean.class,
                    request.queryParams("id"));
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }

}
