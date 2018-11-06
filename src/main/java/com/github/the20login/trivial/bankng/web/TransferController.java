package com.github.the20login.trivial.bankng.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.the20login.trivial.bankng.processing.*;
import com.github.the20login.trivial.bankng.util.Result;
import org.rapidoid.annotation.Controller;
import org.rapidoid.annotation.POST;
import org.rapidoid.http.Req;
import org.rapidoid.http.Resp;

import javax.inject.Inject;

@Controller("transfer")
public class TransferController {
    private final Processing processing;

    @Inject
    public TransferController(Processing processing) {
        this.processing = processing;
    }

    @POST("/")
    public Resp transfer(TransferRequest transferRequest, Req req, Resp resp) {
        Result<TransferResult, ProcessingError> result = processing.transfer(transferRequest.sender, transferRequest.receiver, transferRequest.amount);
        if (result.isError()) {
            return resp.code(400).json(result.getError());
        } else {
            return resp.code(200).json(result.getValue());
        }
    }

    public static class TransferRequest {
        @JsonProperty
        Long sender;
        @JsonProperty
        Long receiver;
        @JsonProperty
        Long amount;
    }
}
