package com.parcelstationx.task;
import org.junit.jupiter.api.Test; import java.util.concurrent.atomic.AtomicBoolean; import static org.junit.jupiter.api.Assertions.*;
class NotificationQueueTest { @Test void runsTaskAndCloses() { AtomicBoolean called=new AtomicBoolean(); try(NotificationQueue queue=new NotificationQueue()){queue.submit(()->called.set(true)).join();} assertTrue(called.get()); } }
