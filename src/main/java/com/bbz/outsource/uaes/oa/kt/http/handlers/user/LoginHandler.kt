package com.bbz.outsource.uaes.oa.kt.http.handlers.user


import com.bbz.outsource.uaes.oa.kt.consts.ErrorCode
import com.bbz.outsource.uaes.oa.kt.consts.ErrorCodeException
import com.bbz.outsource.uaes.oa.kt.consts.JsonConsts
import com.bbz.outsource.uaes.oa.kt.consts.RSAKey
import com.bbz.outsource.uaes.oa.kt.coroutineHandler
import com.bbz.outsource.uaes.oa.kt.db.LoginDataProvider
import com.bbz.outsource.uaes.oa.kt.http.handlers.AbstractHandler
import com.bbz.outsource.uaes.oa.kt.util.Base64Utils
import com.bbz.outsource.uaes.oa.kt.util.CustomHashStrategy
import com.bbz.outsource.uaes.oa.kt.util.RSAUtils
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTOptions
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext

class LoginHandler( private val jwtAuthProvider: JWTAuth,dbClient: SQLClient) : AbstractHandler() {
    private val dataProvider:LoginDataProvider = LoginDataProvider(dbClient)

    fun addRouter(mainRouter: Router): Router {
        mainRouter.route("/login").coroutineHandler{ login(it) }
        mainRouter.route("/logout").coroutineHandler{ logout(it) }

        return mainRouter
    }

    private fun logout(ctx: RoutingContext) {
        ctx.response().end()
    }

    private suspend fun login(ctx: RoutingContext) {
        val userJson = ctx.bodyAsJson
        val username = userJson.getString(JsonConsts.USER_NAME) ?: throw ErrorCodeException(ErrorCode.PARAMETER_ERROR, "username is null")
        val password = userJson.getString(JsonConsts.USER_PASSWORD) ?: throw ErrorCodeException(ErrorCode.PARAMETER_ERROR, "password is null")

        var resultSet = dataProvider.login(JsonArray().add(username))
        val p = decodeRsaPassword(password)
        val errorCode = checkUserLogin(resultSet.results, p)
            if (errorCode.isSuccess) {
                val token = jwtAuthProvider.generateToken(
                        JsonObject()
                                .put("success", true)
                                .put("message", "ok")
                                .put("username", username)
                                .put("roles", JsonArray().add("admin")),
                        JWTOptions()
                                .setSubject("uaes oa")
                                .setIssuer("bbz company"))
                ctx.response().putHeader("Authorization", "Bearer " + token).end(token)
            } else {
                throw ErrorCodeException(errorCode)
            }
        }


    /**
     * 解码从客户端传过来的经过非对称加密之后的密码
     *
     * @param password 密码的密文
     * @return 密码明文
     */
    private fun decodeRsaPassword(password: String): String? {
        try {
            val decode = Base64Utils.decode(password)
            return String(RSAUtils.decryptByPrivateKey(decode, RSAKey.PRIVATE_KEY))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return password
    }

    private fun examinePassword(password: String?, storedPassword: String, salt: String): Boolean {
        val cryptPassword = CustomHashStrategy.INSTANCE.cryptPassword(password, salt)
        return storedPassword == cryptPassword
    }

    private fun checkUserLogin(result: MutableList<JsonArray>, password: String?): ErrorCode {
        println(result)
//        return ErrorCode.SUCCESS
        when (result.size) {
            0 -> {
                return ErrorCode.USER_NOT_FOUND
            }
            1 -> {
                val json = result[0]

                return if (examinePassword(password, json.getString(0), json.getString(1)))
                    ErrorCode.SUCCESS
                else {
                    //                    String message = "Invalid username/password [" + authToken.username + "]";
                    //                    // log.warn(message);
                    //                    throw new AuthenticationException(message);
                    ErrorCode.USER_UNAME_PASS_INVALID
                }
            }
            else -> {
                // More than one row returned!
                //                String message = "More than one user row found for user [" + authToken.username + "( "
                //                        + resultList.result().size() + " )]. Usernames must be unique.";
                // log.warn(message);
                throw ErrorCodeException( ErrorCode.USER_NOT_LOGIN,"怎么查出来多个用户")
            }
        }
    }



}