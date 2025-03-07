package org.example.backend.tm;

public interface TransactionManager {
    long begin();   //创建一个事务
    void commit(long xid);   //提交一个事务
    void abort(long xid);    //取消一个事务
    boolean isActive(long xid); //判断事务是否是运行状态
    boolean isCommit(long xid); //判断事务是否已经提交
    boolean isAborted(long xid);    //判断事务是否取消
    void close();   //关闭TM
}
