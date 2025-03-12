package org.example.backend.dm.pagecache;

import org.example.backend.dm.page.MemoryPage;

public interface MemoryPageCache {
    MemoryPage newPage(byte[] data);   //创建一个缓存页面来存储磁盘页面的数据
    MemoryPage getPage(long key)  throws Exception;  //缓存中获取页面
    void release(MemoryPage page);  //将页面从缓存中逐出
    int getPageNumber();  //获取db文件的页面个数
    void flushPage(MemoryPage page);  //将缓存刷新到磁盘
    void truncateByBgno(int maxBgno);
    void close();  //关闭页面缓存池
}
