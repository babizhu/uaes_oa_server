package com.bbz.outsource.uaes.oa.database;

import com.bbz.outsource.uaes.oa.database.attendance.AttendanceDatabaseService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import java.util.List;

/**
 * @author liulaoye
 */
public class DatabaseVercitle extends AbstractVerticle{
  private JDBCClient dbClient;

  @Override
  public void start(){
//        JsonObject config = new JsonObject()
//                .put( "url", "jdbc:mysql://localhost:3306/dlb?useSSL=true" )
//                .put( "user", "root" )
//                .put( "password", "root" )
////                .put( "driverClassName", "com.zaxxer.hikari" )
//                .put( "maximumPoolSize", 30 );


//    url=jdbc:sqlserver://192.168.0.22:1433;DatabaseName=UFDATA_002_2017
//    driverClassName=com.microsoft.sqlserver.jdbc.SQLServerDriver
    JsonObject config = new JsonObject()
        .put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider")
//        .put("driverClassName","com.microsoft.sqlserver.jdbc.SQLServerDriver")//sqlserver
        .put("jdbcUrl", "jdbc:mysql://localhost:3306/uaes_oa?useSSL=true")
//        .put("jdbcUrl", "jdbc:sqlserver://192.168.0.22:1433;DatabaseName=UFDATA_002_2017")
        .put("username", "root")
        .put("password","root")
//                .put("driverClassName", "org.postgresql.Driver")
        .put("maximumPoolSize", 10);

    dbClient = JDBCClient.createShared( vertx, config );
    AttendanceDatabaseService.create(dbClient);
//    dbClient.getConnection( res -> {
//      if( res.succeeded() ) {
//
//        SQLConnection connection = res.result();
//
//
//        connection.query( "SELECT * FROM ask_for_leave", res2 -> {
//          if( res2.succeeded() ) {
//
//
//
//            for( JsonObject entries : res2.result().getRows() ) {
//              System.out.println( entries );
//            }
//            // 用结果集results进行其他操作
//          }
//        } );
//      } else {
//        // 获取连接失败 - 处理失败的情况
////                res.cause().printStackTrace();
//      }
//    } );

    getAll();
    addLeave();
  }

  private void getAll(){
    dbClient.query("select * from ask_for_leave", res -> {
      if (res.succeeded()) {
        ResultSet result = res.result();
        List<JsonArray> pages = result.getResults();
        System.out.println(pages);
        System.out.println(pages.size());
      } else {
        res.cause().printStackTrace();
      }
    });
  }

  private void addLeave(){

    JsonArray data = new JsonArray()
        .add("user")
        .add(1)
        .add(1)
        .add("babizhu");

    String insertSql="INSERT INTO ask_for_leave (user, begin, approval_result, approvaler) VALUES (?,?,?,?)";


      dbClient.updateWithParams(insertSql, data, res -> {
        if (res.succeeded()) {
          System.out.println("ok");
        } else {
          res.cause().printStackTrace();
        }
      });

  }

  public static void main( String[] args ){
    Vertx vertx = Vertx.vertx();


    vertx.deployVerticle( DatabaseVercitle.class.getName() );
  }
}