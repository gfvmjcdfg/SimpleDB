package org.example.backend.dm.pagecache;

import org.example.backend.common.AbstractCache;
import org.example.backend.dm.page.DiskPage;
import org.example.backend.dm.page.Page;
import org.example.backend.dm.page.PageIml;
import org.example.backend.utils.ProgramExit;
import org.example.comon.Error;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageCacheImpl extends AbstractCache<Page>{
    //我们设置的缓存值的最小值
    private static final int MEM_MIN_LIM = 10;
    private RandomAccessFile file;  //导入磁盘页面文件
    private FileChannel fileChannel;  //打开的磁盘页面通道
    private Lock lock;
    //初始为我们读到的磁盘文件中页面个数，我们在创建缓存页面时，这个个数也要同时增加，因为其最终还是要落盘的
    private AtomicInteger pageNumbers;

    /**
     * 读入一个表的db文件来初始化页面缓存
     * @param path
     * @param maxResource
     */
    public PageCacheImpl(String path, int maxResource) {
        super(maxResource);
        this.lock=new ReentrantLock();
        /**
         * 看初始化是不是太小
         */
        if(maxResource<MEM_MIN_LIM){
            ProgramExit.programExit(Error.MemTooSmallException);
        }
        File file1 = new File(path + DiskPage.DB_SUFFIX);
        try {
            if(!file1.createNewFile()){
                ProgramExit.programExit(Error.FileAlreadyExitsException);
            }
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }
        if(!file1.canRead() || !file1.canWrite()){
            ProgramExit.programExit(Error.FileCannotRWException);
        }
        try {
            this.file=new RandomAccessFile(file1,"rw");
            this.fileChannel=this.file.getChannel();
            long length = file.length();
            this.pageNumbers=new AtomicInteger((int)length/PageIml.PAGE_SIZE);
        } catch (IOException e){
            ProgramExit.programExit(e);
        }
    }

    /**
     * 计算页面在文件中的偏移
     * @param key
     * @return
     */
    public static long pageOffset(int key){
        return (long) (key-1) *PageIml.PAGE_SIZE;
    }

    /**
     * 新建一个缓存页面
     * @param data
     * @return
     */
    public Page newPage(byte[] data){
        int num = pageNumbers.incrementAndGet();
        PageIml pageIml = new PageIml(num, data, null);
        //新建的页面要立刻保存到文件中
        flush(pageIml);
        return pageIml;
    }

    /**
     * 从页面缓存获取一个页面
     * @param key
     * @return
     * @throws Exception
     */
    public Page getPage(long key) throws Exception {
        return (Page) get(key);
    }

    /**
     * 从页面缓存释放一个页面
     */
    public void release(Page page){
        int pageNumber = page.getPageNumber();
        release(pageNumber);
    }


    /**
     * 将一个缓存页面保存到文件中
     * @param page
     */
    public void flush(Page page){
        ByteBuffer byteBuffer = ByteBuffer.wrap(page.getData());
        int pageNumber = page.getPageNumber();
        long offset = pageOffset(pageNumber);
        lock.lock();
        try {
            fileChannel.position(offset);
            fileChannel.write(byteBuffer);
            fileChannel.force(false);
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }finally {
            lock.unlock();
        }
    }



    /**
     * 页面不在缓存中时从磁盘中读
     * @param key
     * @return
     */
    public Page getForCache(long key) {
        int pgno=(int) key;
        //文件偏移
        long offset = pageOffset(pgno);
        ByteBuffer byteBuffer = ByteBuffer.allocate(PageIml.PAGE_SIZE);
        lock.lock();
        try {
            fileChannel.position(offset);
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }finally {
            lock.unlock();
        }
        return new PageIml(pgno,byteBuffer.array(),this);
    }


    /**
     *  驱逐完看是否是脏数据，是需要写回到磁盘
     * @param obj
     */
    public void releaseForCache(Page obj) {
        if(obj.isDirty()){
            flush(obj);
            obj.setDirty(false);
        }
    }

    /**
     * 获取当前缓存中个数
     * @return
     */
    public int getPageNumber(){
        return pageNumbers.intValue();
    }


    public void truncateByBgno(int maxPgno) {
        long size = pageOffset(maxPgno + 1);
        try {
            file.setLength(size);
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }
        pageNumbers.set(maxPgno);
    }
}
