package com.sumkor.plugin.page;


public class PageUtil {

    private static final ThreadLocal<Page> LOCAL_PAGE = new ThreadLocal<Page>();

    public static void setPagingParam(int offset, int limit) {
        Page page = new Page(offset, limit);
        LOCAL_PAGE.set(page);
    }

    public static void removePagingParam() {
        LOCAL_PAGE.remove();
    }

    public static Page getPagingParam() {
        return LOCAL_PAGE.get();
    }

}
