package org.lolicode.allmusic.task;

import java.util.concurrent.TimeUnit;

import static org.lolicode.allmusic.Allmusic.EXECUTOR;
import static org.lolicode.allmusic.Allmusic.TIMER;

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
