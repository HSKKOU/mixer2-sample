package org.mixer2.sample.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.xhtml.A;
import org.mixer2.jaxb.xhtml.Div;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.sample.Lang;
import org.mixer2.sample.view.M2staticHelper;
import org.mixer2.sample.view.PageHelper;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PageController {

    private Logger logger = Logger.getLogger(PageController.class);

    @Autowired
    protected Mixer2Engine mixer2Engine;

    protected String indexTemplatePath = "classpath:m2mockup/m2template/index.html";

    @RequestMapping(value = "/")
    public String top(Locale locale) {
        Lang lang = Lang.EN;
        try {
            lang = Lang.valueOf(locale.getLanguage().toUpperCase());
        } catch (IllegalArgumentException e) {
            return "redirect:/en/index.html";
        }
        String redirect = "redirect:/" + lang.toString().toLowerCase()
                + "/index.html";
        logger.debug("# " + redirect);
        return redirect;
    }

    @RequestMapping(value = "/{lang}/**/*.html")
    public ModelAndView show(@PathVariable String lang,
            HttpServletRequest request) throws FileNotFoundException, IOException, TagTypeUnmatchException  {

        logger.debug("# request processing...");
        ModelAndView modelAndView = new ModelAndView();

        // check lang
        try {
            Lang.valueOf(lang.toUpperCase());
        } catch (IllegalArgumentException e) {
            modelAndView.setViewName("redirect:/en/index.html");
            return modelAndView;
        }
        logger.debug("# lang = " + lang);

        // build template file path from URI
        String path = StringUtils.substringAfter(
                request.getRequestURI(), request.getContextPath() + "/" + lang);
        logger.debug("# path = " + path);
        String templatePath = "classpath:m2mockup/m2template" + path;
        logger.debug("# templatePath = " + templatePath);

        // load template
        Html html = mixer2Engine.loadHtmlTemplate(ResourceUtils.getFile(templatePath));

        // replace side menu (except for index.html)
        if (!templatePath.equals(indexTemplatePath)) {
            Html indexHtml = mixer2Engine.loadHtmlTemplate(ResourceUtils
                    .getFile(indexTemplatePath));
            Div sideBarDiv = indexHtml.getById("sidebar", Div.class);
            html.replaceById("sidebar", sideBarDiv);
        }

        // remove other language tags
        PageHelper.removeOtherLangTags(html, lang);

        // replace anchor to top page
        for (A a : html.getDescendants("topPageAnchor", A.class)) {
            a.setHref(request.getContextPath() + "/");
        }

        // remake lang list at page top
        PageHelper.remakeLangList(html, lang, path);

        // replace static file path
        M2staticHelper.replaceM2staticPath(html);

        modelAndView.setViewName("mixer2view");
        modelAndView.addObject("htmlString", mixer2Engine.saveToString(html));
        return modelAndView;
    }

}
