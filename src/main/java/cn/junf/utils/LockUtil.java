package cn.junf.utils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockUtil {
    private static final LockUtil LOCK_UTIL = new LockUtil();

    /**
     * 线性读写锁
     */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 请求锁
     */
    private final ReentrantLock stateLock = new ReentrantLock();

    /**
     * 线性等待队列
     */
    private final Condition writeCondition = stateLock.newCondition();

    private LockUtil(){}

    public static LockUtil getLockUtil(){
        return LOCK_UTIL;
    }

    public ReentrantReadWriteLock getReadWriteLock(){
        return lock;
    }

    public ReentrantLock getStateLock(){
        return stateLock;
    }

    public Condition getWriteCondition(){
        return writeCondition;
    }
}
