package example.server.function;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.Operation;

import org.apache.geode.cache.asyncqueue.internal.AsyncEventQueueImpl;

import org.apache.geode.cache.execute.Function;

import org.apache.geode.internal.cache.DefaultEntryEventFactory;
import org.apache.geode.internal.cache.EntryEventFactory;
import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.EnumListenerEvent;
import org.apache.geode.internal.cache.EventID;
import org.apache.geode.internal.cache.PartitionedRegion;

import org.apache.geode.internal.cache.wan.AbstractGatewaySender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseFunction implements Function {

  private final EntryEventFactory entryEventFactory = new DefaultEntryEventFactory();

  private static final List<Integer> REMOTE_DS_IDS = Collections.singletonList(-1);

  protected EntryEventImpl createEvent(PartitionedRegion pr, Object key, Object value, EventID eventId, long tailKey) {
    // Create the EntryEventImpl
    EntryEventImpl event = this.entryEventFactory.create(pr, Operation.CREATE, key, value, null, true, pr.getCache().getMyId(), false, eventId);

    // Set the event's tail key. If the input tailKey == -1, then this is the primary.
    // The tailKey is set directly in the event in the secondary.
    if (tailKey != -1) {
      event.setTailKey(tailKey);
    }

    // The tailKey is set by handleWANEvent in the event in the primary.
    // handleWANEvent also updates the BucketRegion's most recent tail key.
    pr.getBucketRegion(key).handleWANEvent(event);

    return event;
  }

  protected void deliverToAsyncEventQueue(EntryEventImpl event) {
    // Get the AsyncEventQueue
    String queueId = (String) event.getRegion().getAsyncEventQueueIds().iterator().next();
    AsyncEventQueueImpl queue = (AsyncEventQueueImpl) event.getRegion().getCache().getAsyncEventQueue(queueId);

    // Get the GatewaySender
    AbstractGatewaySender sender = (AbstractGatewaySender) queue.getSender();

    // Distribute the EntryEvent to the GatewaySender
    sender.distribute(EnumListenerEvent.AFTER_CREATE, event, REMOTE_DS_IDS);
  }

  protected void log(Cache cache, String message) {
    cache.getLogger().info(message);
  }

  public String getId() {
    return getClass().getSimpleName();
  }

  public boolean optimizeForWrite() {
    return true;
  }
}
