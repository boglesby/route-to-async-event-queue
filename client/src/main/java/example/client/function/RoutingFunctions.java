package example.client.function;

import example.client.domain.Routable;
import org.apache.geode.internal.cache.EventID;
import org.springframework.data.gemfire.function.annotation.Filter;
import org.springframework.data.gemfire.function.annotation.FunctionId;
import org.springframework.data.gemfire.function.annotation.OnRegion;

import java.util.Set;

@OnRegion(region = "Router")
public interface RoutingFunctions {

  @FunctionId("PrimaryRoutingFunction")
  Object route(@Filter Set<?> keys, Routable routable, EventID eventId);
}