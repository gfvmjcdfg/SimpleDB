package org.example.backend.dm.pagecache;

import org.example.backend.dm.page.Page;

public interface PageCache {
    Page newPage(byte[] data);
    Page getPage(int key);
    void releasePage(Page page);
    int getPageNumber();
    void flushPage(Page page);
    void truncateByBgno(int maxBgno);
}
