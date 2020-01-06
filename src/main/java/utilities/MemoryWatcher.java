package utilities;

import com.sun.management.GarbageCollectionNotificationInfo;
import tsml.classifiers.MemoryWatchable;

import javax.management.ListenerNotFoundException;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.Serializable;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MemoryWatcher implements Debugable, Serializable, MemoryWatchable {

    public synchronized long getMaxMemoryUsageInBytes() {
        return maxMemoryUsageBytes;
    }

    private long maxMemoryUsageBytes = -1;
    private long size = 0;
    private long firstMemoryUsageReading;
    private long usageSum = 0;
    private long usageSumSq = 0;
    private long garbageCollectionTimeInMillis = 0;
    private boolean resumed = false;
    private transient List<NotificationEmitter> emitters;

    private final NotificationListener listener = (notification, handback) -> {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            GarbageCollectionNotificationInfo info = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            long duration = info.getGcInfo().getDuration();
            garbageCollectionTimeInMillis += duration;
//            String action = info.getGcAction();
//            GcInfo gcInfo = info.getGcInfo();
//            long id = gcInfo.getId();
            Map<String, MemoryUsage> memoryUsageInfo = info.getGcInfo().getMemoryUsageAfterGc();
            for (Map.Entry<String, MemoryUsage> entry : memoryUsageInfo.entrySet()) {
//                String name = entry.getKey();
                MemoryUsage memoryUsageSnapshot = entry.getValue();
//                long initMemory = memoryUsage.getInit();
//                long committedMemory = memoryUsage.getCommitted();
//                long maxMemory = memoryUsage.getMax();
                long memoryUsage = memoryUsageSnapshot.getUsed();
                addMemoryUsageReadingBytes(memoryUsage);
            }
        }
    };

    public MemoryWatcher() {

    }

    public MemoryWatcher(boolean start) {
        if(start) {
            resume();
        }
    }

    private synchronized void addMemoryUsageReadingBytes(long usage) {
        if(usage > maxMemoryUsageBytes) {
            maxMemoryUsageBytes = usage;
        }
        // https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Online_algorithm
        if(size == 0) {
            firstMemoryUsageReading = usage;
        }
        size++;
        long diff = usage - firstMemoryUsageReading;
        usageSum += diff;
        usageSumSq += Math.pow(diff, 2);
    }

    public synchronized long getMeanMemoryUsageInBytes() {
        return firstMemoryUsageReading + usageSum / size;
    }

    public synchronized long getVarianceMemoryUsageInBytes() {
        return (usageSumSq - (usageSum * usageSum) / size) / (size - 1);
    }

    public synchronized void resume() {
        if(!resumed) {
            resumed = true;
            emitters = new ArrayList<>();
            // garbage collector for old and young gen
            List<GarbageCollectorMXBean> garbageCollectorBeans = java.lang.management.ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean garbageCollectorBean : garbageCollectorBeans) {
                if(debug) System.out.println("Setting up listener for gc: " + garbageCollectorBean);
                // listen to notification from the emitter
                NotificationEmitter emitter = (NotificationEmitter) garbageCollectorBean;
                emitters.add(emitter);
                emitter.addNotificationListener(listener, null, null);
            }
        }
    }

    public synchronized void pause() {
        resumed = false;
        if(emitters != null) {
            for(NotificationEmitter emitter : emitters) {
                try {
                    emitter.removeNotificationListener(listener);
                } catch (ListenerNotFoundException e) {
                    throw new IllegalStateException("already removed somehow...");
                }
            }
        }
    }

    @Override
    public synchronized boolean isDebug() {
        return debug;
    }

    private boolean debug = false;

    @Override
    public synchronized void setDebug(boolean state) {
        debug = state;
    }

    public synchronized long getGarbageCollectionTimeInMillis() {
        return garbageCollectionTimeInMillis;
    }
}