package com.bbz.outsource.uaes.oa.http;

import com.bbz.outsource.uaes.oa.database.attendance.AttendanceDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

public class HttpServerVerticle extends AbstractVerticle {
  private AttendanceDatabaseService dbService;
  @Override
  public void start(Future<Void> startFuture) throws Exception {
    super.start(startFuture);
    dbService = AttendanceDatabaseService.createProxy(vertx, "db");
    dbService.search(null, System.out::println);
  }
}
