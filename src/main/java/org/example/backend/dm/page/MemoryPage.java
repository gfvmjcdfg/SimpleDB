package org.example.backend.dm.page;

/**
 * 定义存储在缓存中的页面接口
 */
public interface MemoryPage {
    void lock();
    void unlock();
    byte[] getData();
    int getPageNumber();
    boolean isDirty();
    void setDirty(Boolean b);
    void release();
}
