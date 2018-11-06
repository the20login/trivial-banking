package com.github.the20login.trivial.bankng.processing;

import com.github.the20login.trivial.bankng.processing.account.AccountView;
import com.github.the20login.trivial.bankng.util.Result;

import java.util.List;

public interface Processing {
    Result<AccountView, ProcessingError> getAccountInfo(Long accountId);
    Result<AccountView, ProcessingError> createAccount(AccountView newAccount);
    Result<AccountView, ProcessingError> removeAccount(Long accountId);

    Result<AccountView, ProcessingError> deposit(Long accountId, Long amount);
    Result<AccountView, ProcessingError> withdraw(Long accountId, Long amount);

    Result<TransferResult, ProcessingError> transfer(Long payerId, Long payeeId, Long amount);

    Result<List<AccountView>, ProcessingError> getAllAccountsInfo();
}
