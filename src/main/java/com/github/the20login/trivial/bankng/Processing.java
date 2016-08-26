package com.github.the20login.trivial.bankng;

import java.util.Map;

public interface Processing {
    boolean transfer(long from, long to, long amount);
    long accountBalance(long account);
    Map<Long, Long> snapshot();
    long totalBalance();
}
