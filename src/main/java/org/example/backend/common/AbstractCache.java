package org.example.backend.common;

import org.example.comon.Error;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractCache<T> {

    private Map<Long,T> cache; //缓存数据存放的地方

    private Map<Long,Integer> references; //缓存数据的引用计数

    private Map<Long,Boolean> getting; //用于标记某个不在缓存中的数据正在被加载

    private int maxResource; //允许的最大缓存数目

    private int count; //已经存放的缓存数据个数

    private Lock lock;

    public AbstractCache(int maxResource){
        this.maxResource=maxResource;
        cache=new HashMap<>();
        references=new HashMap<>();
        getting=new HashMap<>();
        lock=new ReentrantLock();
    }

    /**
     * 从缓存中获取数据
     * @param key
     * @return
     */
    public T get(long key) throws Exception {
        //数据存在缓存中
        while(true){  //这个while 循环用来想要用的数据其他线程正在使用
            lock.lock();

            //数据不在缓存中，但是数据正在被加载
            if(getting.containsKey(key)){
                lock.unlock();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            //数据在缓存并且数据没有被其他线程获取
            if(cache.containsKey(key)){
                T obj = cache.get(key);
                references.put(key,references.getOrDefault(key,0)+1);
                lock.unlock();
                return obj;
            }

            //数据不在缓存中,先判断是否能获取资源即缓存区已满
            if(maxResource>0 && count==maxResource){
                lock.unlock();
                throw Error.CacheFullException;
            }
            //此时数据不在缓存中，我们需要从磁盘读数据，先将该数据设置成有人在加载，并跳出当前循环
            count++;
            getting.put(key,true);
            break;
        }

        T obj=null;
        try{
            obj=getForCache(key);
        }catch (Exception e){
            //没能获取到数据
            lock.lock();
            getting.remove(key);
            count--;
            lock.unlock();
        }

        //成功从文件系统读到数据，现在将数据放到缓存中
        lock.lock();
        cache.put(key,obj);
        getting.remove(key);
        references.put(key,1);
        //磁盘读取到缓存，要将缓存中个数+1
        count++;
        lock.unlock();

        return obj;
    }

    /**
     * 将数据从缓存中释放
     * @param key
     */
    public void release(long key){
        lock.lock();
        try {
            int refNum = references.get(key)-1;
            //这个缓存数据没有任何引用了，应该从缓存中取出，并将当前最新数据保存到db文件中
            if(refNum==0){
                T obj = cache.get(key);
                releaseForCache(obj);
                cache.remove(key);
                references.remove(key);
                count--;
            }
            else{
                references.put(key,refNum);
            }
        }finally {
            lock.lock();
        }
    }

    /**
     * 关闭缓存
     */
    public void close(){
        lock.lock();
        try {
            Set<Long> keySet = cache.keySet();
            for(long key:keySet){
                T obj = cache.get(key);
                releaseForCache(obj);
                references.remove(key);
                cache.remove(key);
            }
        }finally {
            lock.unlock();
        }

    }

    /**
     * 数据不在缓存中时的获取策略,此时会在getting数组中标记某个资源正在被加载
     * @param key
     * @return
     */
    public abstract T getForCache(long key);

    /**
     * 资源不在缓存时的驱逐策略,会将数据写回到db文件中，保证db文件中数据是最新的
     * @param obj
     */
    public abstract void releaseForCache(T obj);

}
