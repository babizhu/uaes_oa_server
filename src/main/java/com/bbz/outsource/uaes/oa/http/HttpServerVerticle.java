package com.bbz.outsource.uaes.oa.http;

import com.bbz.outsource.uaes.oa.database.attendance.AttendanceDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class HttpServerVerticle extends AbstractVerticle {
  private AttendanceDatabaseService dbService;
  @Override
  public void start(Future<Void> startFuture) throws Exception {
    super.start(startFuture);
    dbService = AttendanceDatabaseService.createProxy(vertx, "db");
//    dbService.search(null, res->{
//      if(res.succeeded()){
//        System.out.println(res.result());
//      }else {
//        res.cause().printStackTrace();
//      }
//    });
    dbService.doSomething("abcd",res-> System.out.println(res));
    dbService.query(new JsonObject().put("key","abcdefg"), System.out::println);
  }
}
