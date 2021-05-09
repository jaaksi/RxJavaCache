package org.jaaksi.rxcachedemo.model;

public class BannerBean {
    public String desc;

    public Integer id;

    public String imagePath;

    public Integer isVisible;

    public Integer order;

    public String title;

    public Integer type;

    public String url;

    @Override
    public String toString() {
        return "BannerBean{" + "desc='" + desc + '\'' + ", id=" + id + ", imagePath='" + imagePath + '\'' + ", isVisible=" +
               isVisible + ", order=" + order + ", title='" + title + '\'' + ", type=" + type + ", url='" + url + '\'' + '}';
    }
}
