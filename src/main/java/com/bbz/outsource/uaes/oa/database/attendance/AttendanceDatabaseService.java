package com.bbz.outsource.uaes.oa.database.attendance;


import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import java.util.List;

/**
 * @author liulaoye
 */
@ProxyGen
@VertxGen
public interface AttendanceDatabaseService{

  /**
   * create
   *
   * @param dbClient  dbClient
   * @return this
   */
  @GenIgnore
  static AttendanceDatabaseService create(JDBCClient dbClient) {
    return new AttendanceDatabaseServiceImpl(dbClient);
  }

  /**
   * createProxy
   * @param vertx     vertx
   * @param address   address
   * @return  AttendanceDatabaseServiceVertxEBProxy
   */
  static AttendanceDatabaseService createProxy(Vertx vertx, String address) {
    return new AttendanceDatabaseServiceVertxEBProxy(vertx, address);
  }


  @Fluent
  AttendanceDatabaseService search(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler);


  @Fluent
  AttendanceDatabaseService query(JsonObject condition, Handler<AsyncResult<String>> resultHandler);


//  @Fluent
//  AttendanceDatabaseService delete(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler);
//
//

  /**
   * 添加请假记录
   * @param leave  写入数据库的记录
   * @param resultHandler resultHandler  返回一个新增记录的ID
   *
   * @return              this
   */
  @Fluent
  AttendanceDatabaseService create(JsonArray leave,Handler<AsyncResult<Integer>> resultHandler);
//
//
//  @Fluent
//  AttendanceDatabaseService update(JsonObject condition, Handler<AsyncResult<List<JsonArray>>> resultHandler);


  void doSomething(String str,Handler<AsyncResult<String >> resultHandler);
}
