package example.server.function;

import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.partition.PartitionRegionHelper;

public class AssignBucketsFunction implements Function {

  public void execute(FunctionContext context) {
    // Get the arguments
    String[] arguments = (String[]) context.getArguments();
    String regionName = arguments[0];

    //Assign the buckets to the region
    PartitionRegionHelper.assignBucketsToPartitions(context.getCache().getRegion(regionName));
    context.getCache().getLogger().info("Assigned buckets to regionName=" + regionName);

    // Return the result
    context.getResultSender().lastResult(true);
  }

  public String getId() {
    return getClass().getSimpleName();
  }
}
