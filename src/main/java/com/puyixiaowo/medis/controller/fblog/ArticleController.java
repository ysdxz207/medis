package com.puyixiaowo.medis.controller.fblog;

import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.ArticleBean;
import com.puyixiaowo.medis.bean.admin.CategoryBean;
import com.puyixiaowo.medis.bean.admin.UserBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.constants.Constants;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.ArticleService;
import com.puyixiaowo.medis.utils.DBUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Moses
 * @date 2017-08-08 13:48:12
 * 文章
 */
public class ArticleController extends BaseController {

    /**
     * 文章列表
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions(value = {"article:view"})
    public static Object articles(Request request, Response response) {

        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/article/article_list.html"));
        }

        PageBean pageBean = getPageBean(request);
        try {
            ArticleBean params = getParamsEntity(request, ArticleBean.class, false);
            List<ArticleBean> list =
                    ArticleService.selectArticleList(
                            getParamsEntity(request, ArticleBean.class, false));
            pageBean.setList(list);
            int count = ArticleService.selectCount(params);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }

    /**
     * 添加或修改文章
     *
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions(value = {"article:edit"})
    public static String edit(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            Map<String, Object> model = new HashMap<>();
            ArticleBean articleBean = getParamsEntity(request, ArticleBean.class, false);
            if (articleBean.getId() > 0) {
                //编辑
                articleBean = DBUtils.selectOne(ArticleBean.class, "select * from article where id = :id", articleBean);
                model.put("model", articleBean);
            }

            //分类列表
            model.put("categoryList", DBUtils.selectList(CategoryBean.class,
                    "select * from category", null));

            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(model,
                            "admin/article/article_edit.html"));
        }

        ResponseBean responseBean = new ResponseBean();
        try {
            ArticleBean articleBean = getParamsEntity(request, ArticleBean.class, true);

            UserBean currentUser = request.session().attribute(Constants.SESSION_USER_KEY);
            articleBean.setCreator(currentUser.getLoginname());
            articleBean.setCreateDate(System.currentTimeMillis() / 1000);
            if (articleBean.getId() != null) {
                articleBean.setLastUpdateDate(System.currentTimeMillis() / 1000);
            }
            DBUtils.insertOrUpdate(articleBean);
        } catch (Exception e) {
            responseBean.errorMessage(e.getMessage());
        }

        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"article:delete"})
    public static String delete(Request request, Response response) {
        ResponseBean responseBean = new ResponseBean();

        try {
            String ids = request.queryParams("id");
            DBUtils.deleteByIds(ArticleBean.class,
                    ids);
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }
}
