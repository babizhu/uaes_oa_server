package com.bbz.outsource.uaes.oa.database.attendance;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import java.util.List;

/**
 * @author liulaoye
 */
public class AttendanceDatabaseServiceImpl implements AttendanceDatabaseService {

  private final JDBCClient dbClient;

  public AttendanceDatabaseServiceImpl(JDBCClient dbClient) {
    this.dbClient = dbClient;
  }

  @Override
  public AttendanceDatabaseService search(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler) {
    dbClient.query("select * from ask_for_leave", res -> {
      if (res.succeeded()) {
        ResultSet result = res.result();
        List<JsonArray> pages = result.getResults();
        System.out.println(pages);
        System.out.println(pages.size());
        resultHandler.handle(Future.succeededFuture(pages));
      } else {
        res.cause().printStackTrace();
      }
    });
    return this;
  }
}
