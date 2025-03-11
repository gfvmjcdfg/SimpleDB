package org.example.backend.tm;

import org.example.backend.utils.ProgramExit;
import org.example.comon.Error;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TransactionManagerImpl implements TransactionManager{

    //设置xID文件头长度
    static final int XID_HEAD_LENGTH=8;
    //设置每个事务记录的长度
    private static final int XID_FIELD_LENGTH=1;
    //设置事务的三个状态码
    private static final byte XID_STATE_ACTIVATE=0;
    private static final byte XID_STATE_COMMITED=1;
    private static final byte XID_STATE_ABORTED=2;
    //超级事务的id(不使用)，永远为comitted状态
    private static final long SUPER_XID=0;
    //xid文件的后缀
    private static final String STRING_XID_FILE_SUFFIX=".xid";

    //读取到的xid文件
    private RandomAccessFile xidFile;
    //nio传输xid文件的通道
    private FileChannel fileChannel;
    //记录xid文件管理的事务数目
    private long xidCount;

    private Lock lock;

    //新建我们的事务管理，并且在新建完之后要检查xid文件的合法性
    public TransactionManagerImpl(RandomAccessFile xidFile,FileChannel fileChannel){
        this.fileChannel=fileChannel;
        this.xidFile=xidFile;
        lock=new ReentrantLock();
        checkXidFile();
    }

    /**
     * 检查xid文件是否合法
     * 合法指的是xid文件是要前面8个字符是事务总个数,这个要与8个字符后面的字符个数要一致
     */
    public void checkXidFile(){
        long fileLen=0;
        try{
            //记录xid文件长度
            fileLen=xidFile.length();
        }catch (IOException e) {
            ProgramExit.programExit(e);
        }

        //xid文件小于8个字节肯定不对
        if(fileLen<XID_HEAD_LENGTH){
            ProgramExit.programExit(Error.XID_FILE_FORMAT_ERROR);
        }

        //检查事务数是不是对的上
        long transactionNum=0;
        ByteBuffer buffer= ByteBuffer.allocate(XID_HEAD_LENGTH);
        try {
            fileChannel.read(buffer);
            buffer.flip();
            //直接调用Buffer流获得事务的个数
            transactionNum+=buffer.getLong();
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }

        if(transactionNum!=fileLen-8){
            ProgramExit.programExit(Error.XID_FILE_FORMAT_ERROR);
        }

    }

    /**
     * 返回一个事务在xid文件中的位置,即在第几个字节，从0开始计数
     * 0号事务不需要记录，因此位置要为（xid-1）*XID_FIELD_LENGTH
     * @param xid
     * @return
     */
    public long calXidPosition(long xid){
        return XID_HEAD_LENGTH+(xid-1)*XID_FIELD_LENGTH;
    }

    /**
     * 用于更新对应xid事务的状态,用于被修改事务状态的其他函数调用
     */
    public void updateXidStatus(long xid,byte status){
        //获取事务在xid文件中偏移量
        long offst=calXidPosition(xid);
        try {
            fileChannel.position(offst);
            //创建我们即将写入的数据
            byte[] bytes=new byte[XID_FIELD_LENGTH];
            bytes[0]=status;
            ByteBuffer wrap = ByteBuffer.wrap(bytes, 0, XID_FIELD_LENGTH);
            fileChannel.write(wrap);
            //强制立刻更新到磁盘，但是不必更新元数据
            fileChannel.force(false);
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }
    }

    /**
     * 更新xidCount的值，同时更新xid文件头
     */
    public void increseXidCount(){
        //由于++操作不是一个原子操作，这里上了个锁
        lock.lock();
        xidCount++;
        ByteBuffer buffer = ByteBuffer.allocate(8).putLong(xidCount);
        try {
            fileChannel.position(0);
            fileChannel.write(buffer);
        } catch (IOException e) {
            ProgramExit.programExit(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 检查xid事务的状态
     * @param xid
     * @return
     */
    public byte getXidStatus(long xid){
        long offset = calXidPosition(xid);
        try {
            fileChannel.position(offset);
            ByteBuffer byteBuffer = ByteBuffer.allocate(XID_FIELD_LENGTH);
            fileChannel.read(byteBuffer);
            byteBuffer.flip();
            return byteBuffer.get();
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }
        return -1;
    }


    /**
     * 这里还有个问题，这里修改完xidcount++，我们应该也要修改xid文件的head
     * @return
     */
    public long begin() {
        lock.lock();
        try {
            //调用方法将xid++，并更新xid文件头
            increseXidCount();
            updateXidStatus(xidCount,XID_STATE_ACTIVATE);
            return xidCount;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 提交事务
     * @param xid
     */
    public void commit(long xid) {
        updateXidStatus(xid,XID_STATE_COMMITED);
    }

    /**
     * 取消/回滚事务
     * @param xid
     */
    public void abort(long xid) {
        updateXidStatus(xid,XID_STATE_ABORTED);
    }

    @Override
    public boolean isActive(long xid) {
        byte xidStatus = getXidStatus(xid);

        return xidStatus==XID_STATE_ACTIVATE;
    }

    @Override
    public boolean isCommit(long xid) {

        byte xidStatus = getXidStatus(xid);

        return xidStatus==XID_STATE_COMMITED;
    }

    @Override
    public boolean isAborted(long xid) {
        byte xidStatus = getXidStatus(xid);
        return xidStatus==XID_STATE_ABORTED;
    }

    /**
     * 关闭TM模块功能，即将打开的xid文件关闭
     */
    public void close() {
        try {
            fileChannel.close();
            xidFile.close();
        } catch (IOException e) {
            ProgramExit.programExit(e);
        }
    }
}
