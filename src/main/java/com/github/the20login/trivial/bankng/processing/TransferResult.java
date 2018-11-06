package com.github.the20login.trivial.bankng.processing;

import com.github.the20login.trivial.bankng.processing.account.AccountView;

public class TransferResult {
    private final AccountView sender;
    private final AccountView receiver;
    private final Long amount;

    public TransferResult(AccountView sender, AccountView receiver, Long amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public AccountView getSender() {
        return sender;
    }

    public AccountView getReceiver() {
        return receiver;
    }

    public Long getAmount() {
        return amount;
    }
}
