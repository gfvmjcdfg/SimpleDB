package org.example.backend.dm.pagecache;

import org.example.backend.common.AbstractCache;
import org.example.backend.dm.page.Page;
import org.example.backend.dm.page.MemoryPage;
import org.example.backend.dm.page.MemoryPageIml;
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

/**
 * 缓存池的实现
 *
 * 我一开始想过是不是这里需要自己弄一个缓存数据存放，因为父类的我们看不到，后来一想我们没必要访问其
 * 因为存放缓存的代码已经在抽象类中已经实现了
 */
public class MemoryPageCacheImpl extends AbstractCache<MemoryPage> implements MemoryPageCache {
    //我们设置的缓存值的最小值
    private static final int MEM_MIN_LIM = 10;
    private RandomAccessFile file;  //导入磁盘页面文件
    private FileChannel fileChannel;  //打开的磁盘页面通道
    private Lock lock;
    //初始为我们读到的磁盘文件中页面个数，我们在创建缓存页面时，这个个数也要同时增加，因为其最终还是要落盘的
    private AtomicInteger pageNumbers;

    /**
     * 初始化一个页面缓存池，并提供访问db文件的通道
     * @param path
     * @param maxResource
     */
    public MemoryPageCacheImpl(String path, int maxResource) {
        super(maxResource);
        this.lock=new ReentrantLock();
        /**
         * 看初始化是不是太小
         */
        if(maxResource<MEM_MIN_LIM){
            ProgramExit.programExit(Error.MemTooSmallException);
        }
        File file1 = new File(path + Page.DB_SUFFIX);
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
            this.pageNumbers=new AtomicInteger((int)length/ Page.PAGE_SIZE);
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
        return (long) (key-1) * Page.PAGE_SIZE;
    }

    /**
     * 我们在读磁盘页面时，需要把磁盘页面转为缓存页面
     * @param data
     * @return
     */
    public MemoryPage newPage(byte[] data){
        int num = pageNumbers.incrementAndGet();
        MemoryPage page = new MemoryPageIml(num, data, null);
        //新建的页面要立刻保存到文件中
        flushPage(page);
        return page;
    }

    /**
     * 从页面缓存获取一个页面
     * @param key
     * @return
     * @throws Exception
     */
    public MemoryPage getPage(long key) throws Exception {
        return (MemoryPage) get(key);
    }

    /**
     * 从页面缓存释放一个页面
     */
    public void release(MemoryPage page){
        int pageNumber = page.getPageNumber();
        release(pageNumber);
    }


    /**
     * 将一个缓存页面保存到文件中
     * @param page
     */
    public void flush(MemoryPage page){
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

    public void flushPage(MemoryPage page) {
        flush(page);
    }


    /**
     * 页面不在缓存中时从磁盘中读
     * @param key
     * @return
     */
    public MemoryPage getForCache(long key) {
        int pgno=(int) key;
        //文件偏移
        long offset = pageOffset(pgno);
        ByteBuffer byteBuffer = ByteBuffer.allocate(Page.PAGE_SIZE);
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
        return new MemoryPageIml(pgno,byteBuffer.array(),this);
    }


    /**
     *  驱逐完看是否是脏数据，是需要写回到磁盘
     * @param obj
     */
    public void releaseForCache(MemoryPage obj) {
        if(obj.isDirty()){
            flushPage(obj);
            obj.setDirty(false);
        }
    }

    /**
     * 获取db文件的页面个数
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
