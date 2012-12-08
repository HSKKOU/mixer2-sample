package org.mixer2.sample.view;

import org.mixer2.jaxb.xhtml.A;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Li;
import org.mixer2.jaxb.xhtml.Ul;
import org.mixer2.sample.Lang;
import org.mixer2.sample.util.RequestUtil;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;

public class PageHelper {

    public static void removeOtherLangTags(Html html, String lang) {
        for (Lang l : Lang.values()) {
            if (lang.equals(l.toString().toLowerCase())) {
                continue;
            }
            String target = "lang-" + l.toString().toLowerCase();
            html.removeDescendants(target);
        }
    }

    public static void remakeLangList(Html html, String lang, String path)
            throws TagTypeUnmatchException {

        Ul langList = html.getById("langList", Ul.class);
        langList.getLi().clear();
        for (Lang l : Lang.values()) {
            Li li = new Li();
            if (lang.equals(l.toString().toLowerCase())) {
                li.getContent().add(l.getName());
            } else {
                A a = new A();
                a.getContent().add(l.getName());
                a.setHref(RequestUtil.getRequest().getContextPath() + "/"
                        + l.toString().toLowerCase() + path);
                li.getContent().add(a);
            }
            langList.getLi().add(li);
        }

    }

}
