package customer.service;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Uses ScheduledExecutorService to run and schedule tasks
 *
 * Created by roman rasskazov on 12.06.2015.
 */
public class RestScheduler {

    private static Logger log = Logger.getLogger(RestScheduler.class);

    //run only one thread with item updates
    private static final int EXECUTOR_POOL_SIZE = 1;
    //delay between update uns
    private static final int PERIOD_IN_MILLISECONDS = 2000;

    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(EXECUTOR_POOL_SIZE);

    private static List<ScheduledFuture> tasks = new ArrayList<ScheduledFuture>();

    /**
     * Runs task periodically with configured delay
     */
    public static void schedule(Runnable task){
        tasks.add(executor.scheduleWithFixedDelay(task, 0, PERIOD_IN_MILLISECONDS, TimeUnit.MILLISECONDS));
    }

    /**
     * Runs task in separate thread
     */
    public static void runTask(Runnable task){
         tasks.add(executor.schedule(task, 0, TimeUnit.MILLISECONDS));
    }

    /**
     * Stops update process thread to not leave it after applicatin exit
     */
    public static void releaseThread (){
        log.info("Cancel scheduler");
        for (ScheduledFuture task : tasks) {
            try {
                task.cancel(false);
            } catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }
}
