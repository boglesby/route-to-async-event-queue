package example.server.aeq;

import org.apache.geode.cache.Cache;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.asyncqueue.AsyncEvent;
import org.apache.geode.cache.asyncqueue.AsyncEventListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RoutingAsyncEventListener implements AsyncEventListener {

  private final AtomicLong numberOfEvents;

  private final AtomicLong numberOfPossibleDuplicateEvents;

  private final Cache cache;

  public RoutingAsyncEventListener() {
    this.numberOfEvents = new AtomicLong();
    this.numberOfPossibleDuplicateEvents = new AtomicLong();
    this.cache = CacheFactory.getAnyInstance();
  }

  public boolean processEvents(List<AsyncEvent> events) {
    StringBuilder builder = new StringBuilder();
    builder
      .append("RoutingAsyncEventListener: Processing ")
      .append(events.size())
      .append(" events (total=")
      .append(this.numberOfEvents.addAndGet(events.size()))
      .append("; possibleDuplicate=")
      .append(this.numberOfPossibleDuplicateEvents.addAndGet(getNumberOfPossibleDuplicates(events)))
      .append(")");
    events.forEach((event) -> process(builder, event));
    this.cache.getLogger().info(builder.toString());
    return true;
  }

  private void process(StringBuilder builder, AsyncEvent event) {
    builder
      .append("\n\t")
      .append("key=")
      .append(event.getKey())
      .append("; value=")
      .append(event.getDeserializedValue())
      .append("; possibleDuplicate=")
      .append(event.getPossibleDuplicate());
  }

  private long getNumberOfPossibleDuplicates(List<AsyncEvent> events) {
    return events
      .stream()
      .filter(AsyncEvent::getPossibleDuplicate)
      .count();
  }
}
