package com.github.the20login.trivial.bankng.stm;

import com.github.the20login.trivial.bankng.Processing;
import scala.concurrent.stm.Ref;
import scala.runtime.AbstractFunction1;

import static scala.concurrent.stm.japi.STM.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StmProcessing implements Processing {
    private final Map<Long, Ref.View<Long>> accounts;

    public StmProcessing(Map<Long, Long> initial) {
        accounts = initial.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry->newRef(entry.getValue())
                ));
    }

    @Override
    public boolean transfer(long from, long to, final long amount) {
        if (from == to)
            return false;
        Ref.View<Long> fromRef = accounts.get(from);
        Ref.View<Long> toRef = accounts.get(to);

        return atomic(()->{
            if (fromRef.get() < amount)
                return false;
            fromRef.transform(new AbstractFunction1<Long, Long>() {
                @Override
                public Long apply(Long fromBalance) {
                    return fromBalance - amount;
                }
            });
            toRef.transform(new AbstractFunction1<Long, Long>() {
                @Override
                public Long apply(Long toBalance) {
                    return toBalance + amount;
                }
            });
            return true;
        });
    }

    @Override
    public long accountBalance(long account) {
        return accounts.get(account).get();
    }

    @Override
    public Map<Long, Long> snapshot() {
        return atomic(()-> {
            return accounts.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().get()
                    ));
        });
    }

    @Override
    public long totalBalance() {
        return snapshot().values().stream().collect(Collectors.summingLong(val->val));
    }
}
