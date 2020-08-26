package example.client.service;

import example.client.domain.CusipHelper;
import example.client.domain.Routable;
import example.client.domain.Trade;
import example.client.function.RoutingFunctions;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.internal.cache.EventID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Random;

@Service
public class TradeService {

  @Autowired
  private RoutingFunctions routingFunctions;

  @Autowired
  private Region routerRegion;

  @Autowired
  private GemFireCache cache;

  protected static final Random random = new Random();

  protected static final Logger logger = LoggerFactory.getLogger(TradeService.class);

  public void load(int numEntries) {
    logger.info("Loading {} entries", numEntries);
    long start = 0, end = 0;
    start = System.currentTimeMillis();
    for (int i=0; i<numEntries; i++) {
      Trade trade = new Trade(String.valueOf(i), CusipHelper.getCusip(), random.nextInt(100), new BigDecimal(BigInteger.valueOf(random.nextInt(100000)), 2));
      this.routerRegion.put(trade.getId(), trade);
      logger.info("Saved trade={}", trade);
    }
    end = System.currentTimeMillis();
    logger.info("Loaded {} entries in {} ms", numEntries, end - start);
  }

  public void route(int numEntries) {
    logger.info("Routing {} entries", numEntries);
    long start = 0, end = 0;
    start = System.currentTimeMillis();
    for (int i=0; i<numEntries; i++) {
      Routable routable = new Trade(String.valueOf(i), CusipHelper.getCusip(), random.nextInt(100), new BigDecimal(BigInteger.valueOf(random.nextInt(100000)), 2));
      EventID eventId = new EventID(this.cache.getDistributedSystem());
      Object result = this.routingFunctions.route(Collections.singleton(routable.getRoutingKey()), routable, eventId);
      logger.info("Routed routable={}; eventId={}; result={}", routable, eventId, result);
    }
    end = System.currentTimeMillis();
    logger.info("Routed {} entries in {} ms", numEntries, end - start);
  }

  public void routeForever(int numEntries) {
    int i = 0;
    long start = 0, end = 0;
    start = System.currentTimeMillis();
    while (true) {
      Routable routable = new Trade(String.valueOf(random.nextInt(numEntries)), CusipHelper.getCusip(), random.nextInt(100), new BigDecimal(BigInteger.valueOf(random.nextInt(100000)), 2));
      EventID eventId = new EventID(this.cache.getDistributedSystem());
      this.routingFunctions.route(Collections.singleton(routable.getRoutingKey()), routable, eventId);
      if ((++i + 1) % 1000 == 0) {
        end = System.currentTimeMillis();
        logger.info("Routed {} entries in {} ms", numEntries, end - start);
        start = System.currentTimeMillis();
      }
    }
  }

  public void dumpClientMetadata() {
    ClientMetadataHelper.printMetadata(this.cache, this.routerRegion);
  }
}
