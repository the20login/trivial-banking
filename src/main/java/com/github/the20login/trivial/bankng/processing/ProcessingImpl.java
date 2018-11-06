package com.github.the20login.trivial.bankng.processing;

import com.github.the20login.trivial.bankng.processing.account.AccountRecord;
import com.github.the20login.trivial.bankng.processing.account.AccountView;
import com.github.the20login.trivial.bankng.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProcessingImpl implements Processing {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessingImpl.class);
    private final ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();
    private final AtomicLong idGenerator;
    private final Map<Long, AccountRecord> accounts;

    public ProcessingImpl() {
        this(Collections.emptyList());
    }

    ProcessingImpl(Collection<AccountRecord> initial) {
        accounts = new ConcurrentHashMap<>(initial.size());
        Long maxId = initial.stream()
                .peek(accountRecord -> accounts.compute(accountRecord.getId(),
                        (id, record) -> {
                            if (record != null)
                                throw new IllegalArgumentException("Duplicated id: " + id);
                            return accountRecord;
                        }))
                .map(AccountRecord::getId)
                .max(Long::compare)
                .orElse(0L);

        idGenerator = new AtomicLong(maxId);
    }

    @Override
    @SuppressWarnings("unchecked cast")
    public Result<TransferResult, ProcessingError> transfer(Long senderId, Long receiverId, Long amount) {
        if (amount == null) {
            String message = "No amount specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.INCORRECT_AMOUNT, message));
        }

        if (senderId == null) {
            String message = "No sender id specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, message));
        }

        if (receiverId == null) {
            String message = "No sender id specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, message));
        }

        if (senderId.equals(receiverId)) {
            AccountRecord record = accounts.get(senderId);
            if (record == null) {
                String message = "Attempt to self-transfer, non existing account id " + senderId;
                LOG.warn(message);
                return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
            }
            String message = "Attempt to self-transfer, account id " + senderId;
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.SELF_TRANSFER, message));
        }



        AccountRecord senderRecord = accounts.get(senderId);
        AccountRecord receiverRecord = accounts.get(receiverId);

        if (senderRecord == null) {
            String message = "Sender account " + senderId + " not found";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
        }

        if (receiverRecord == null) {
            String message = "Receiver account " + senderId + " not found";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
        }

        if (amount <= 0) {
            String message = "Attempt to transfer negative amount " + amount + " from " + senderId + " to " + receiverId;
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.INCORRECT_AMOUNT, message));
        }

        return withGlobalLock(true, () -> {
            return withTwoAccountsWriteLock(senderRecord, receiverRecord, (sender, receiver) -> {
                if (sender.getBalance() - amount < 0) {
                    String message = "Sender " + senderId + " doesn't have enough funds, requested amount is " + amount;
                    LOG.warn(message);
                    return Result.<TransferResult, ProcessingError>error(new ProcessingError(OperationErrorCode.INSUFFICIENT_FUNDS, message));
                }
                sender.decrementBalance(amount);
                receiver.incrementBalance(amount);
                LOG.info("Successfully transferred " + amount + " from " + senderId + " to " + receiverId);
                return Result.<TransferResult, ProcessingError>value(new TransferResult(sender.getView(), receiver.getView(), amount));
            });
        });
    }

    @Override
    public Result<AccountView, ProcessingError> getAccountInfo(Long accountId) {
        AccountRecord record = accounts.get(accountId);
        if (record == null) {
            String message = "Requested account " + accountId + " not found";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
        }
        return record.withReadLock(account -> Result.value(account.getView()));
    }

    @Override
    public Result<AccountView, ProcessingError> createAccount(AccountView newAccount) {
        Optional<String> validationResult = validateNewRecord(newAccount);
        if (validationResult.isPresent()) {
            LOG.warn("Can't create account, validation error: {}", validationResult.get());
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, validationResult.get()));
        }
        Long id = idGenerator.incrementAndGet();
        AccountRecord newRecord = AccountRecord.fromView(id, newAccount);
        return withGlobalLock(true, () -> {
            //get view beforehand, since account could be modified right after putting
            AccountView viewToReturn = newRecord.withReadLock(AccountRecord.ReadableAccount::getView);

            AccountRecord existingAccount = accounts.putIfAbsent(id, newRecord);
            if (existingAccount != null) {
                LOG.warn("Attempt to override account " + id);
                //this error is impossible with current implementation,
                // but could happen later, when code would be changed, so let it be to help with future bugs
                return Result.<AccountView, ProcessingError>error(new ProcessingError(OperationErrorCode.DUPLICATE_RECORD, ""));
            }
            LOG.info("Account " + id + " successfully created");
            return Result.<AccountView, ProcessingError>value(viewToReturn);
        });
    }

    @Override
    public Result<AccountView, ProcessingError> removeAccount(Long accountId) {
        if (accountId == null) {
            String message = "No account id specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, message));
        }

        return withGlobalLock(true, () -> {
            AccountRecord record = accounts.remove(accountId);
            if (record == null) {
                String message = "Account to remove " + accountId + " not found";
                LOG.warn(message);
                return Result.<AccountView, ProcessingError>error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
            }
            LOG.info("Account " + accountId + " successfully removed");
            return Result.<AccountView, ProcessingError>value(record.withReadLock(AccountRecord.ReadableAccount::getView));
        });
    }

    @Override
    public Result<AccountView, ProcessingError> deposit(Long accountId, Long amount) {
        if (amount == null) {
            String message = "No amount specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.INCORRECT_AMOUNT, message));
        }

        if (accountId == null) {
            String message = "No account id specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, message));
        }

        AccountRecord record = accounts.get(accountId);
        if (record == null) {
            String message = "Account " + accountId + " not found";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
        }
        return withGlobalLock(true, () -> record.withWriteLock(account -> {
            account.incrementBalance(amount);
            LOG.warn("Increase account " + accountId + " balance by " + amount);
            return Result.value(account.getView());
        }));
    }

    @Override
    public Result<AccountView, ProcessingError> withdraw(Long accountId, Long amount) {
        if (amount == null) {
            String message = "No amount specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.INCORRECT_AMOUNT, message));
        }

        if (accountId == null) {
            String message = "No account id specified";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.VALIDATION_ERROR, message));
        }

        AccountRecord record = accounts.get(accountId);
        if (record == null) {
            String message = "Account " + accountId + " not found";
            LOG.warn(message);
            return Result.error(new ProcessingError(OperationErrorCode.ACCOUNT_NOT_FOUND, message));
        }

        return withGlobalLock(true, () -> record.withWriteLock(account -> {
            if (account.getBalance() - amount < 0) {
                String message = "Account " + accountId + " doesn't have enough funds to withdraw, requested amount is " + amount;
                LOG.warn(message);
                return Result.<AccountView, ProcessingError>error(new ProcessingError(OperationErrorCode.INSUFFICIENT_FUNDS, message));
            }
            account.decrementBalance(amount);
            LOG.warn("Decrease account " + accountId + " balance by " + amount);
            return Result.<AccountView, ProcessingError>value(account.getView());
        }));
    }

    @Override
    public Result<List<AccountView>, ProcessingError> getAllAccountsInfo() {
        return withGlobalLock(false, () -> {
            List<AccountView> accountsViews = accounts.values().stream()
                    .map(account -> account.withReadLock(AccountRecord.ReadableAccount::getView))
                    .collect(Collectors.toList());

            return Result.value(accountsViews);
        });
    }

    private Optional<String> validateNewRecord(AccountView view) {
        if (view.getId() != null) {
            return Optional.of("Can't use explicit id for new records");
        }

        if (view.getOwner().isEmpty()) {
            return Optional.of("Owner is missing");
        }

        if (view.getBalance() < 0) {
            return Optional.of("Balance can't be negative");
        }

        return Optional.empty();
    }

    private <T> T withGlobalLock(boolean parallel, Supplier<T> handler) {
        Lock lock;
        if (parallel)
            lock = globalLock.readLock();
        else
            lock = globalLock.writeLock();

        lock.lock();
        try {
            return handler.get();
        } finally {
            lock.unlock();
        }
    }

    private <T> T withTwoAccountsWriteLock(
            AccountRecord senderRecord,
            AccountRecord receiverRecord,
            BiFunction<AccountRecord.WritableAccount, AccountRecord.WritableAccount, T> operationHandler) {
        if (senderRecord.getId() > receiverRecord.getId()) {
            return senderRecord.withWriteLock(writableSender -> {
                return receiverRecord.withWriteLock(writableReceiver -> {
                    return operationHandler.apply(writableSender, writableReceiver);
                });
            });
        } else {
            return receiverRecord.withWriteLock(writableReceiver -> {
                return senderRecord.withWriteLock(writableSender -> {
                    return operationHandler.apply(writableSender, writableReceiver);
                });
            });
        }
    }
}
