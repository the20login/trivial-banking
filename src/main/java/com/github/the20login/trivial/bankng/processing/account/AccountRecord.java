package com.github.the20login.trivial.bankng.processing.account;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

public class AccountRecord {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Long id;
    private Long balance;
    private String owner;

    private final ReadableAccount readWrapper = new ReadableAccount(this);
    private final WritableAccount writeWrapper = new WritableAccount(this);

    public static AccountRecord fromView(Long id, AccountView view) {
        return new AccountRecord(id, view.getBalance(), view.getOwner());
    }

    public AccountRecord(Long id, Long balance, String owner) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public <T> T withReadLock(Function<ReadableAccount, T> withLock) {
        lock.readLock().lock();
        try {
            return withLock.apply(readWrapper);
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T withWriteLock(Function<WritableAccount, T> withLock) {
        lock.writeLock().lock();
        try {
            return withLock.apply(writeWrapper);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static class ReadableAccount {
        protected final AccountRecord accountRecord;

        private ReadableAccount(AccountRecord accountRecord) {
            this.accountRecord = accountRecord;
        }

        public Long getId() {
            return accountRecord.id;
        }

        public Long getBalance() {
            return accountRecord.balance;
        }

        public String getOwner() {
            return accountRecord.owner;
        }

        public AccountView getView() {
            return new AccountView(
                    accountRecord.id,
                    accountRecord.balance,
                    accountRecord.owner);
        }
    }

    public static class WritableAccount extends ReadableAccount {
        private WritableAccount(AccountRecord accountRecord) {
            super(accountRecord);
        }

        public void setBalance(Long balance) {
            accountRecord.balance = balance;
        }

        public Long incrementBalance(Long amount) {
            accountRecord.balance += amount;
            return accountRecord.balance;
        }

        public Long decrementBalance(Long amount) {
            accountRecord.balance -= amount;
            return accountRecord.balance;
        }

        public void setOwner(String owner) {
            accountRecord.owner = owner;
        }

        public void setEnabled(boolean enabled) {
        }
    }
}
