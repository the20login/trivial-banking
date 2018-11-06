package com.github.the20login.trivial.bankng.processing.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountView {
    private final Long id;
    private final Long balance;
    private final String owner;

    @JsonCreator
    public AccountView(
            @JsonProperty("id") Long id,
            @JsonProperty(value = "balance", required = true) Long balance,
            @JsonProperty(value = "owner", required = true) String owner) {
        this.id = id;
        this.balance = balance;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public Long getBalance() {
        return balance;
    }

    public String getOwner() {
        return owner;
    }

}
