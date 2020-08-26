package example.server.function;

import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.RegionFunctionContext;

import org.apache.geode.cache.partition.PartitionRegionHelper;

import org.apache.geode.distributed.DistributedMember;

import org.apache.geode.internal.cache.EntryEventImpl;
import org.apache.geode.internal.cache.EventID;
import org.apache.geode.internal.cache.PartitionedRegion;

import java.util.Arrays;
import java.util.Set;

public class PrimaryRoutingFunction extends BaseFunction {

  public void execute(FunctionContext context) {
    RegionFunctionContext rfc = (RegionFunctionContext) context;

    // Get the key
    Object key = rfc.getFilter().iterator().next();

    // Get the arguments
    Object[] arguments = (Object[]) context.getArguments();
    Object value = arguments[0];
    EventID eventId = (EventID) arguments[1];

    // Get the PartitionedRegion
    PartitionedRegion pr = (PartitionedRegion) rfc.getDataSet();
    log(context.getCache(), "PrimaryRoutingFunction processing args= " + Arrays.toString(arguments));

    // Create the event
    EntryEventImpl event = createEvent(pr, key, value, eventId, -1);

    // Route the event to the secondary members
    routeToSecondaryMembers(pr, event);

    // Add the event to the local queue
    deliverToAsyncEventQueue(event);

    // Return the result
    context.getResultSender().lastResult(true);
  }

  private void routeToSecondaryMembers(PartitionedRegion pr, EntryEventImpl event) {
    // Get the redundant members for this key
    Set<DistributedMember> redundantMembers = PartitionRegionHelper.getRedundantMembersForKey(pr, event.getKey());

    // Create and execute the SecondaryRoutingFunction on those members if necessary
    if (!redundantMembers.isEmpty()) {
      Object[] args = new Object[] {pr.getFullPath(), event.getKey(), event.getValue(), event.getEventId(), event.getTailKey()};
      try {
        FunctionService
          .onMembers(redundantMembers)
          .setArguments(args)
          .execute("SecondaryRoutingFunction")
          .getResult();
      } catch (FunctionException e) {
        // If the remote member is killed, a FunctionException is thrown. Log a warning and continue.
        pr.getCache().getLogger()
          .warning("PrimaryRoutingFunction caught exception executing SecondaryRoutingFunction for key=" + event.getKey() + "; value=" + event.getNewValue(), e);
      }
    }
  }
}

