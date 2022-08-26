package cn.mine.community.util;

import java.util.List;

public class Page<T> {
    private int pageNum = 1;
    private int pageSize = 10;
    private int navigatePages = 8;
    private long total;
    private List<T> list;
    private int size;
    private String path;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        if (pageNum >= 1)
            this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize >= 1 && pageSize <= 20)
            this.pageSize = pageSize;
    }

    public int getNavigatePages() {
        return navigatePages;
    }

    public void setNavigatePages(int navigatePages) {
        if (navigatePages >= 1 && navigatePages <= 20)
            this.navigatePages = navigatePages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
        this.size = list.size();
    }

    public int getSize() {
        return this.size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPages() {
        if (total % pageSize == 0)
            return (int) (total / pageSize);
        else
            return (int) (total / pageSize + 1);
    }

    public int getPrePage() {
        return pageNum == 1 ? 1 : (pageNum - 1);
    }

    public int getNextPage() {
        int pages = getPages();
        return pageNum == pages ? pages : (pageNum + 1);
    }

    public int getFromPage() {
        int pages = getPages();
        if (pages <= navigatePages)
            return 1;
        else {
            int from = pageNum - navigatePages / 2;
            int to = pageNum + navigatePages / 2;

            if (from < 1) {
                return 1;
            } else if (to > pages) {
                return pages + 1 - navigatePages;
            } else {
                return from;
            }
        }
    }

    public int getToPage() {
        int pages = getPages();
        if (pages <= navigatePages)
            return pages;
        else {
            int from = pageNum - navigatePages / 2;
            int to = pageNum + navigatePages / 2;

            if (from < 1) {
                return navigatePages;
            } else if (to > pages) {
                return pages;
            } else {
                return from - 1 + navigatePages;
            }
        }
    }
}
