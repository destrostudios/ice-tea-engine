package com.destrostudios.icetea.core.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ThreadUtil {

    public static void waitForCompletion(Iterable<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException("Error while waiting for future completion", ex);
            }
        }
    }
}
