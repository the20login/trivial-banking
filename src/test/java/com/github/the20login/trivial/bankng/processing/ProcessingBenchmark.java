package com.github.the20login.trivial.bankng.processing;

import com.github.the20login.trivial.bankng.processing.account.AccountRecord;
import com.github.the20login.trivial.bankng.util.Result;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.SampleTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1)
@Threads(Threads.MAX)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
public class ProcessingBenchmark {
    private static final int ACCOUNTS_COUNT = 100000;
    private static final Long INITIAL_BALANCE = Long.MAX_VALUE;
    private Processing processing;

    @Setup
    public void init() {
        List<AccountRecord> initial = new ArrayList<>(ACCOUNTS_COUNT);
        for (Long i = 1L; i <= ACCOUNTS_COUNT; i++) {
            initial.add(new AccountRecord(i, INITIAL_BALANCE,"human_" + i));
        }
        processing = new ProcessingImpl(initial);
    }

    @Benchmark
    public Result<TransferResult, ProcessingError> transfer_random() {
        long from = ThreadLocalRandom.current().nextLong(ACCOUNTS_COUNT) + 1;
        long to = ThreadLocalRandom.current().nextLong(ACCOUNTS_COUNT) + 1;
        return processing.transfer(from, to, 1L);
    }

    @Benchmark
    public Result<TransferResult, ProcessingError> transfer_hot_account() {
        long from = 1;
        long to = ThreadLocalRandom.current().nextLong(ACCOUNTS_COUNT) + 1;
        return processing.transfer(from, to, 1L);
    }
}