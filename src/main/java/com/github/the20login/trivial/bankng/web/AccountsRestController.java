package com.github.the20login.trivial.bankng.web;

import com.github.the20login.trivial.bankng.processing.*;
import com.github.the20login.trivial.bankng.processing.account.AccountView;
import com.github.the20login.trivial.bankng.util.Result;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.GET;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;

import javax.inject.Inject;
import java.util.List;

@Controller("accounts")
public class AccountsRestController {
    private final Processing processing;

    @Inject
    public AccountsRestController(Processing processing) {
        this.processing = processing;
    }

    @GET("/")
    public Resp getAllAccounts(Req req, Resp resp) {
        Result<List<AccountView>, ProcessingError> result = processing.getAllAccountsInfo();
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }

    @POST("/")
    public Resp createAccount(AccountView newAccount, Req req, Resp resp) {
        Result<AccountView, ProcessingError> result = processing.createAccount(newAccount);
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }

    @GET("/{id:\\d+}")
    public Resp getAccountInfo(Long id, Req req, Resp resp) {
        Result<AccountView, ProcessingError> result = processing.getAccountInfo(id);
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }

    @POST("/accounts/{id:\\d+}/withdraw")
    public Resp withdraw(Long id, Long amount, Req req, Resp resp) {
        Result<AccountView, ProcessingError> result = processing.withdraw(id, amount);
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }

    @POST("/accounts/{id:\\d+}/deposit")
    public Resp deposit(Long id, Long amount, Req req, Resp resp) {
        Result<AccountView, ProcessingError> result = processing.deposit(id, amount);
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }
}
