package org.lolicode.nekomusic.task;

import java.util.concurrent.TimeUnit;

import static org.lolicode.nekomusic.NekoMusic.*;

public class ServerStop {
    public static void onServerStop() {
        TIMER.cancel();
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(3, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }
    }
}
