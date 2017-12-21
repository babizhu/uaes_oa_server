package com.bbz.outsource.uaes.oa;

import com.bbz.outsource.uaes.oa.database.DatabaseVercitle;
import com.bbz.outsource.uaes.oa.http.HttpServerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainVerticle extends AbstractVerticle {

//    private static final int PORT = 8000;

//    private HttpClient httpClient;


  @Override
  public void start(Future<Void> startFuture) {
    DeploymentOptions dbOptions = new DeploymentOptions().setConfig(config().getJsonObject("mongo"));

    Future<String> dbVerticleDeployment = Future.future();
    vertx.deployVerticle(new DatabaseVercitle(), dbOptions, dbVerticleDeployment.completer());
    dbVerticleDeployment.compose(id -> {
      log.debug("开始初始化HttpServerVerticle");
      Future<String> httpVerticleDeployment = Future.future();
      DeploymentOptions options = new DeploymentOptions().setConfig(
          config().getJsonObject("server").
              put("mongo", config().getJsonObject("mongo")));

      vertx.deployVerticle(
//                    "com.srxk.car.user.behavioranalysis.http.HttpServerVerticle",
          new HttpServerVerticle(),
          options,
          httpVerticleDeployment.completer());
      return httpVerticleDeployment;

    }).setHandler(ar -> {
      if (ar.succeeded()) {
        startFuture.complete();
      } else {
        startFuture.fail(ar.cause());
      }
    });
  }

  public static void main(String[] args) throws IOException {

    Student student = new Student("刘老爷",23);
    log.debug(student.toString());

    final VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setBlockedThreadCheckInterval(1000000);
    Vertx vertx = Vertx.vertx(vertxOptions);

    DeploymentOptions options = new DeploymentOptions();
    options.setInstances(1);

    String content = new String(Files.readAllBytes(Paths.get("resources/application-conf.json")));
    final JsonObject config = new JsonObject(content);

    log.info(config.toString());
    options.setConfig(config);

    vertx.deployVerticle(MainVerticle.class.getName(), options, res -> {
      if (res.succeeded()) {
        log.info(" server started ");
      } else {
        res.cause().printStackTrace();
      }
    });

  }
//        final VertxOptions vertxOptions = new VertxOptions();
//        vertxOptions.setBlockedThreadCheckInterval( 1000000 );
//        Vertx vertx = Vertx.vertx( vertxOptions );
//
//        DeploymentOptions options = new DeploymentOptions();
////        options.setInstances( 1 );
//
//        vertx.deployVerticle( MainVerticle.class.getName(), options, res -> {
//            if( res.succeeded() ) {
//                System.out.println( "web server started at port " + PORT + ", please click http://localhost:" + PORT + " to visit!" );
//            } else {
//                res.cause().printStackTrace();
//            }
//        } );
//    }
}