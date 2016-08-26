package com.github.the20login.trivial.bankng.keylock;

import com.github.the20login.trivial.bankng.Processing;
import de.jkeylockmanager.manager.KeyLockManager;
import de.jkeylockmanager.manager.KeyLockManagers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class KeylockProcessing implements Processing {
    private final KeyLockManager lockManager = KeyLockManagers.newLock();
    private final Map<Long, Long> accounts;
    private final ReadWriteLock globalLock = new ReentrantReadWriteLock();

    public KeylockProcessing(Map<Long, Long> initial)
    {
        accounts = new HashMap<>(initial);
    }

    public boolean transfer(long from, long to, long amount) {
        if (from == to)
            return false;
        long first, second;
        if (from > to) {
            first = to;
            second = from;
        }
        else {
            first = from;
            second = to;
        }
        try {
            globalLock.readLock().lock();
            return lockManager.executeLocked(first, () -> lockManager.executeLocked(second, () -> {
                long fromBalance = accounts.get(from);
                long toBalance = accounts.get(to);
                if (fromBalance < amount)
                    return false;
                fromBalance -= amount;
                toBalance += amount;
                accounts.put(from, fromBalance);
                accounts.put(to, toBalance);
                return true;
            }));
        }
        finally {
            globalLock.readLock().unlock();
        }
    }

    public long accountBalance(long account){
        try {
            globalLock.readLock().lock();
            return lockManager.executeLocked(account, ()->{
                return accounts.get(account);
            });
        }
        finally {
            globalLock.readLock().unlock();
        }
    }

    public Map<Long, Long> snapshot(){
        try {
            globalLock.writeLock().lock();
            return new HashMap<>(accounts);
        }
        finally {
            globalLock.writeLock().unlock();
        }
    }

    public long totalBalance(){
        try {
            globalLock.writeLock().lock();
            return accounts.values().stream().collect(Collectors.summingLong(value -> value));
        }
        finally {
            globalLock.writeLock().unlock();
        }
    }
}
