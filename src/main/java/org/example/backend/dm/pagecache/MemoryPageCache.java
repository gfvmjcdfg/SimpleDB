package org.example.backend.dm.pagecache;

import org.example.backend.dm.page.MemoryPage;

public interface MemoryPageCache {
    MemoryPage newPage(byte[] data);
    MemoryPage getPage(long key)  throws Exception;
    void release(MemoryPage page);
    int getPageNumber();
    void flushPage(MemoryPage page);
    void truncateByBgno(int maxBgno);
}
