package example.client.domain;

import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.gemfire.mapping.annotation.Region;

import java.math.BigDecimal;

@Data
@Region("Router")
public class Trade implements Routable {

  @Id
  @NonNull
  private final String id;

  @NonNull
  private final String cusip;

  @NonNull
  private final Integer shares;

  @NonNull
  private final BigDecimal price;

  @Override
  @Reference
  public Object getRoutingKey() {
    return this.id;
  }
}
