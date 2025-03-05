package org.example.backend.tm;

public class TransactionManagerImpl implements TransactionManager{

    //设置xID文件头长度
    static final int XID_HEAD_LENGTH=8;
    //设置每个事务记录的长度
    private static final int XID_FIELD_LENGTH=1;
    //设置事务的三个状态码
    private static final byte TRAN_STATE_ACTIVATE=0;
    private static final byte TRAN_STATE_COMMITED=1;
    private static final byte TRAN_STATE_ABORTED=2;
    //超级事务的id(不使用)，永远为comitted状态
    private static final long SUPER_XID=0;
    //xid文件的后缀
    private static final String STRING_XID_FILE_SUFFIX=".xid";

    @Override
    public long begin() {
        return 0;
    }

    @Override
    public boolean commit(long xID) {
        return false;
    }

    @Override
    public boolean abort(long xID) {
        return false;
    }

    @Override
    public boolean isActive(long xID) {
        return false;
    }

    @Override
    public boolean isCommit(long xID) {
        return false;
    }

    @Override
    public boolean isAborted(long xID) {
        return false;
    }

    @Override
    public void close() {

    }
}
