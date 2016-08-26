package com.github.the20login.trivial.bankng;

import com.github.the20login.trivial.bankng.stm.StmProcessing;

import org.rapidoid.lambda.OneParamLambda;
import org.rapidoid.lambda.ThreeParamLambda;
import org.rapidoid.setup.On;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private final Processing processing;

    public Main() throws IOException {
        Map<Long, Long> map = new HashMap<>();
        for (long i = 0; i < 10000; i++){
            map.put(i, 1_000_000_000L);
        }
        processing =  new StmProcessing(map);

        On.address("0.0.0.0").port(8080);

        On.get("/transfer/{from}/{to}/{amount}").plain(new ThreeParamLambda<String, Long, Long, Long>() {
            @Override
            public String execute(Long from, Long to, Long amount) throws Exception {
                if (processing.transfer(from, to , amount))
                    return "Transferred " + amount + " money from " + from + " to " + to;
                return "Not transferred";
            }
        });

        On.get("/bankBalance").plain(processing::totalBalance);
        On.get("/balance").plain(()->{
            StringBuilder builder = new StringBuilder();
            processing.snapshot().entrySet().stream()
                .forEach(entry-> {
                    builder.append(entry.getKey());
                    builder.append("\t");
                    builder.append(entry.getValue());
                    builder.append("\n");
                });
            return builder.toString();
        });
        On.get("/balance/{account}").plain(new OneParamLambda<String, Long>() {
            @Override
            public String execute(Long account) throws Exception {
                return "Account balance " + processing.accountBalance(account);
            }
        });

        System.out.println("\nRunning! Point your browers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new Main();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }
}
