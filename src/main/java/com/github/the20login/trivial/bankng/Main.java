package com.github.the20login.trivial.bankng;

import com.github.the20login.trivial.bankng.keylock.KeylockProcessing;
import com.github.the20login.trivial.bankng.stm.StmProcessing;
import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main extends NanoHTTPD {
    private static final Response NOT_FOUND = newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found");
    private final Processing processing;

    public Main() throws IOException {
        super(8080);
        Map<Long, Long> map = new HashMap<>();
        for (long i = 0; i < 10000; i++){
            map.put(i, 1_000_000_000L);
        }
        processing =  new StmProcessing(map);

        this.setAsyncRunner(new BoundRunner(Executors.newFixedThreadPool(4)));
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new Main();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String[] path = session.getUri().split("/");
        if (path.length < 2)
            return NOT_FOUND;
        switch (path[1]) {
            case "transfer":
            {
                if (path.length != 5)
                    return NOT_FOUND;
                long from, to, amount;
                try {
                    from = Long.valueOf(path[2]);
                    to = Long.valueOf(path[3]);
                    amount = Long.valueOf(path[4]);
                }
                catch (NumberFormatException e)
                {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Can't parse");
                }

                if (processing.transfer(from, to, amount))
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Transferred " + amount + " money from " + from + " to " + to);
                else
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Not transferred");
            }
            case "bankBalance": {
                if (path.length == 2)
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Total balance " + processing.totalBalance());
                else
                    return NOT_FOUND;
            }
            case "balance":
            {
                if (path.length == 2) {
                    StringBuilder builder = new StringBuilder();
                    processing.snapshot().entrySet().stream()
                            .forEach(entry-> {
                                builder.append(entry.getKey());
                                builder.append("\t");
                                builder.append(entry.getValue());
                                builder.append("\n");
                            });
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, builder.toString());
                }
                else if (path.length == 3){
                    long account;
                    try {
                        account = Long.valueOf(path[2]);
                    }
                    catch (NumberFormatException e)
                    {
                        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Can't parse");
                    }
                    return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Total balance " + processing.accountBalance(account));
                }
                else
                    return NOT_FOUND;
            }
            default:
                return NOT_FOUND;
        }
    }
}
