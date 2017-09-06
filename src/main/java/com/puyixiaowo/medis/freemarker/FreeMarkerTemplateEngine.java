package com.puyixiaowo.medis.freemarker;

import freemarker.template.*;
import spark.ModelAndView;
import spark.TemplateEngine;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

public class FreeMarkerTemplateEngine extends TemplateEngine {

    private Configuration configuration;

    public FreeMarkerTemplateEngine() {
        this.configuration = createFreemarkerConfiguration();
    }

    @Override
    public String render(ModelAndView modelAndView) {
        try {
            StringWriter stringWriter = new StringWriter();

            Template template = configuration.getTemplate(modelAndView.getViewName());
            template.process(modelAndView.getModel(), stringWriter);

            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        } catch (TemplateException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Configuration createFreemarkerConfiguration() {
        Configuration retVal = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        retVal.setEncoding(Locale.CHINA, "UTF-8");
        try {
            retVal.setSharedVariable("base", "");
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
        retVal.setClassForTemplateLoading(FreeMarkerTemplateEngine.class, "/page");
        return retVal;
    }
}