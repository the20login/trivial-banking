package com.github.the20login.trivial.bankng.processing;

import com.github.the20login.trivial.bankng.processing.account.AccountRecord;
import com.github.the20login.trivial.bankng.processing.account.AccountView;
import com.github.the20login.trivial.bankng.util.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingImplTest {
    private Processing processing;

    @BeforeEach
    void init() {
        List<AccountRecord> records = new ArrayList<>();
        records.add(new AccountRecord(1L, 10L, "1"));
        records.add(new AccountRecord(2L, 20L, "2"));
        processing = new ProcessingImpl(records);
    }

    @Test
    void transfer() {
        Result<TransferResult, ProcessingError> result = processing.transfer(1L, 2L, 1L);
        assertFalse(result.isError());
        TransferResult transferResult = result.getValue();
        assertEquals((Long)1L, transferResult.getAmount());
        assertEquals((Long)1L, transferResult.getSender().getId());
        assertEquals((Long)9L, transferResult.getSender().getBalance());
        assertEquals((Long)21L, transferResult.getReceiver().getBalance());

        Result<AccountView, ProcessingError> infoResult = processing.getAccountInfo(1L);
        assertFalse(result.isError());
        assertEquals((Long)9L, infoResult.getValue().getBalance());

        infoResult = processing.getAccountInfo(2L);
        assertFalse(result.isError());
        assertEquals((Long)21L, infoResult.getValue().getBalance());
    }

    @Test
    void getAccountInfo_noAccount() {
        Result<AccountView, ProcessingError> result = processing.getAccountInfo(3L);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.ACCOUNT_NOT_FOUND, result.getError().getErrorCode());
    }

    @Test
    void getAccountInfo_existingAccount() {
        Result<AccountView, ProcessingError> result = processing.getAccountInfo(2L);
        assertFalse(result.isError());
        AccountView account = result.getValue();
        assertEquals((Long)2L, account.getId());
        assertEquals((Long)20L, account.getBalance());
        assertEquals("2", account.getOwner());
    }

    @Test
    void deposit() {
        Result<AccountView, ProcessingError> result = processing.deposit(2L, 1L);
        assertFalse(result.isError());
        AccountView account = result.getValue();
        assertEquals((Long)2L, account.getId());
        assertEquals((Long)21L, account.getBalance());
        assertEquals("2", account.getOwner());
    }

    @Test
    void deposit_noAccount() {
        Result<AccountView, ProcessingError> result = processing.deposit(3L, 1L);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.ACCOUNT_NOT_FOUND, result.getError().getErrorCode());
    }

    @Test
    void withdraw() {
        Result<AccountView, ProcessingError> result = processing.deposit(2L, 1L);
        assertFalse(result.isError());
        AccountView account = result.getValue();
        assertEquals((Long)2L, account.getId());
        assertEquals((Long)21L, account.getBalance());
        assertEquals("2", account.getOwner());
    }

    @Test
    void withdraw_insufficientFunds() {
        Result<AccountView, ProcessingError> result = processing.withdraw(2L, 30L);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.INSUFFICIENT_FUNDS, result.getError().getErrorCode());

        result = processing.getAccountInfo(2L);
        assertFalse(result.isError());
        AccountView account = result.getValue();
        assertEquals((Long)20L, account.getBalance());
    }

    @Test
    void withdraw_noAccount() {
        Result<AccountView, ProcessingError> result = processing.withdraw(3L, 1L);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.ACCOUNT_NOT_FOUND, result.getError().getErrorCode());
    }

    @Test
    void createAccount() {
        AccountView newAccount = new AccountView(null, 15L, "15");
        Result<AccountView, ProcessingError> result = processing.createAccount(newAccount);
        assertFalse(result.isError());
        AccountView registeredAccount = result.getValue();
        assertEquals(newAccount.getBalance(), registeredAccount.getBalance());
        assertEquals(newAccount.getOwner(), registeredAccount.getOwner());
        assertEquals((Long)3L, registeredAccount.getId());
    }

    @Test
    void createAccount_withId() {
        AccountView newAccount = new AccountView(10L, 15L, "15");
        Result<AccountView, ProcessingError> result = processing.createAccount(newAccount);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.VALIDATION_ERROR, result.getError().getErrorCode());
    }

    @Test
    void removeAccount() {
        Result<AccountView, ProcessingError> result = processing.removeAccount(2L);
        assertFalse(result.isError());
        AccountView removedAccount = result.getValue();
        assertEquals((Long)2L, removedAccount.getId());
        assertEquals("2", removedAccount.getOwner());
        assertEquals((Long)20L, removedAccount.getBalance());

        Result<AccountView, ProcessingError> infoResult = processing.getAccountInfo(2L);
        assertTrue(infoResult.isError());
        assertEquals(OperationErrorCode.ACCOUNT_NOT_FOUND, infoResult.getError().getErrorCode());
    }

    @Test
    void removeAccount_noAccount() {
        Result<AccountView, ProcessingError> result = processing.removeAccount(3L);
        assertTrue(result.isError());
        assertEquals(OperationErrorCode.ACCOUNT_NOT_FOUND, result.getError().getErrorCode());
    }

    @Test
    void getAllAccountsInfo() {
        Result<List<AccountView>, ProcessingError> result = processing.getAllAccountsInfo();
        assertFalse(result.isError());
        List<AccountView> accounts = result.getValue();
        assertEquals(2, accounts.size());

        for (AccountView account: accounts) {
            if (account.getId() == 1L) {
                assertEquals((Long)10L, account.getBalance());
                assertEquals("1", account.getOwner());
            } else if (account.getId() == 2L) {
                assertEquals((Long)20L, account.getBalance());
                assertEquals("2", account.getOwner());
            } else {
                fail("Unexpected account");
            }
        }
    }
}