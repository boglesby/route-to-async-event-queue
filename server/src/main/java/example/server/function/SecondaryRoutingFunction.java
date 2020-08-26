package example.server.function;

import org.apache.geode.cache.execute.FunctionContext;

import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.EventID;
import org.apache.geode.internal.cache.PartitionedRegion;

import java.util.Arrays;

public class SecondaryRoutingFunction extends BaseFunction {

  public void execute(FunctionContext context) {
    // Get the arguments
    Object[] arguments = (Object[]) context.getArguments();
    String regionName = (String) arguments[0];
    Object key = arguments[1];
    Object value = arguments[2];
    EventID eventId = (EventID) arguments[3];
    long tailKey = (Long) arguments[4];
    
    // Get the PartitionedRegion
    PartitionedRegion pr = (PartitionedRegion) context.getCache().getRegion(regionName);
    log(context.getCache(),"SecondaryRoutingFunction processing args= " + Arrays.toString(arguments));

    // Create the event
    EntryEventImpl event = createEvent(pr, key, value, eventId, tailKey);

    // Add the event to the local queue
    deliverToAsyncEventQueue(event);

    // Return the result
    context.getResultSender().lastResult(true);
  }
}
