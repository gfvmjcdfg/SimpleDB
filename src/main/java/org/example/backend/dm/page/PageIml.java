package org.example.backend.dm.page;

import org.example.backend.dm.pagecache.PageCacheImpl;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageIml implements Page {
    //将缓存中页面大小定为8k
    public static int PAGE_SIZE=1 << 13;
    private int pageNumber;  //页号
    private byte[] data;   //页面中存储的数据
    private Lock lock;    //该页的锁
    private boolean dirty;  //是否是脏页
    private PageCacheImpl pageCache;  // 页面缓存，这里是为了方便我们拿到页面后更快决定是否释放页面

    public PageIml(int pageNumber, byte[] data, PageCacheImpl pageCache) {
        this.pageNumber = pageNumber;
        this.data = data;
        this.pageCache = pageCache;
        this.lock=new ReentrantLock();
        this.dirty=false;
    }

    /**
     * 对页面上锁
     */
    public void lock() {
        lock.lock();
    }

    /**
     * 释放页面锁
     */
    public void unlock() {
        lock.unlock();
    }

    /**
     * 获取页面的数据
     * @return
     */
    public byte[] getData() {
        return this.data;
    }

    /**
     * 获取这个页面是第几个页面
     * @return
     */
    public int getPageNumber() {
        return this.pageNumber;
    }

    /**
     * 判断页面是否是脏页面
     * @return
     */
    public boolean isDirty() {
        return this.dirty;
    }

    /**
     * 将页面设置成脏页面
     */
    public void setDirty(Boolean b) {
        this.dirty=b;
    }

    /**
     * 将页面从缓存中驱逐，具体逻辑由缓存池实现
     */
    public void release() {
        pageCache.release(this);
    }
}
