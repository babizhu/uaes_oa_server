package com.bbz.outsource.uaes.oa.kt.http.handlers.auth

import com.bbz.outsource.uaes.oa.kt.consts.ErrorCode
import com.bbz.outsource.uaes.oa.kt.consts.ErrorCodeException
import com.bbz.outsource.uaes.oa.kt.db.LoginDataProvider
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.anno.RequirePermissions
import com.bbz.outsource.uaes.oa.kt.http.handlers.auth.anno.RequireRoles
import com.google.common.reflect.ClassPath
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.JWTAuthHandler
import io.vertx.ext.web.handler.impl.AuthHandlerImpl
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


class CustomJwt(authProvider: JWTAuth, dbClient: SQLClient) : AuthHandlerImpl(authProvider), JWTAuthHandler {
    /**
     * 角色到权限的映射
     */

    private val options = JsonObject()

    init {
    }

    override fun setAudience(audience: List<String>): JWTAuthHandler {
        options.put("audience", JsonArray(audience))
        return this
    }

    override fun setIssuer(issuer: String): JWTAuthHandler {
        options.put("issuer", issuer)
        return this
    }

    override fun setIgnoreExpiration(ignoreExpiration: Boolean): JWTAuthHandler {
        options.put("ignoreExpiration", ignoreExpiration)
        return this
    }

    override fun handle(context: RoutingContext) {

        val request = context.request()

        //

        val authorization = request.headers().get(HttpHeaders.AUTHORIZATION)
        var token: String? = null

        if (authorization != null) {
            val parts = authorization.split(" ")
            if (parts.size == 2) {
                val scheme = parts[0]
                val credentials = parts[1]

                if (BEARER == scheme) {
                    token = credentials
                }
            } else {
                log.warn("Format is Authorization: Bearer [token]")
                context.fail(401)
                return
            }
        } else {
            log.warn("No Authorization header was found")
            throw ErrorCodeException(ErrorCode.USER_NOT_LOGIN)
            //      return;
        }

        val authInfo = JsonObject().put("jwt", token).put("options", options)

        authProvider.authenticate(authInfo) { res ->
            if (res.succeeded()) {
                val user = res.result()
                context.setUser(user)
                val session = context.session()
                session?.regenerateId()
                authorise(user, context)
            } else {
                log.warn("JWT decode failure", res.cause())
                throw ErrorCodeException(ErrorCode.USER_NOT_LOGIN,"JWT decode failure")
            }
        }
    }


    private fun authorise(user: User, ctx: RoutingContext) {
        //    log.debug("检测权限,用户的权限：" + user.principal().getJsonArray("roles"));
        val uri = ctx.request().uri()
        log.debug("访问的地址：" + uri)
        log.debug("需要的权限：" + AUTH_MAP[uri])
        //        super.authorise( user, context );
        if (doIsPermitted(user.principal().getString("roles"), AUTH_MAP[uri])) {
            ctx.next()
        } else {
            throw ErrorCodeException(ErrorCode.USER_PERMISSION_DENY)
        }
    }


    private fun doIsPermitted(userRoles: String, uriPermissionSet: Set<String>?): Boolean {

        //        Set<String> userRoles = user.getRoles();
        //        Set<String> permissions = user.getPermissions();
        //        return userRoles.contains( "admin" ) || permissionOrRole.contains( userRoles ) || permissionOrRole.contains( permissions );
        if (uriPermissionSet == null) {
            return true
        }

        for (role in userRoles.split(",")) {
            if (role == "admin") {
                return true
            }
            val permisstions = rolesPermissionsMap[role] ?: return false
            for (permisstion in permisstions) {
                if(uriPermissionSet.contains(permisstion)){
                    return true
                }
            }
        }
        return false

    }

    override fun parseCredentials(routingContext: RoutingContext, handler: Handler<AsyncResult<JsonObject>>) {

    }

    companion object {

        private val log = LoggerFactory.getLogger(CustomJwtAuthHandlerImpl::class.java)
        private val HANDLER_PACKAGE_BASE = "com.bbz.outsource.uaes.oa.kt.http.handlers"
        /**
         * 仅供内部使用，原则上初始化之后不允许修改，否则可能造成多线程竞争，如果需要修改，可考虑采用vertx.sharedData()
         */
        val AUTH_MAP = HashMap<String, Set<String>>()
        private val rolesPermissionsMap = HashMap<String, Set<String>>()
        /**
         * private static final Pattern BEARER = Pattern.compile( "^Bearer$", Pattern.CASE_INSENSITIVE );         *
         */
        private val BEARER = "Bearer"

        init {
            try {
                val classpath = ClassPath.from(Thread.currentThread().contextClassLoader)
                for (classInfo in classpath.getTopLevelClassesRecursive(HANDLER_PACKAGE_BASE)) {
                    parseClass(classInfo.load())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            log.info(AUTH_MAP.toString())

        }

        private fun parseClass(clazz: Class<*>) {
            val methods = clazz.declaredMethods

            for (method in methods) {
                if (method.isAnnotationPresent(RequirePermissions::class.java) || method.isAnnotationPresent(RequireRoles::class.java)) {
                    val clazzName = getClassName(clazz)
                    val permisstionSet = HashSet<String>()

                    if (method.isAnnotationPresent(RequirePermissions::class.java)) {
                        permisstionSet.addAll(getSetFromStr(method.getDeclaredAnnotation(RequirePermissions::class.java).value))
                    }
                    //                if( method.isAnnotationPresent( RequireRoles.class ) ) {
                    //                    roleAndPermisstionSet.addRoles( getSetFromStr( method.getDeclaredAnnotation( RequireRoles.class ).value() ) );
                    //                }
                    ///api/trade/getTradeInfo
                    val url = clazzName + "/" + method.name
                    AUTH_MAP.put(url, permisstionSet)
                }
            }
        }

        /**
         * 把逗号分割的字符串转成一个Set
         *
         * @param str 要分割的字符串
         * @return set
         */
        private fun getSetFromStr(str: String): Set<String> {
            return str.split(",").toSet()
        }

        /**
         * 按照规则生成class的name
         * 去掉包前缀PACKAGE_BASE = "web.handler.impl"
         * 去掉类名中的Handler
         * 转换为小写
         *
         * @param clazz class
         * @return class modelName
         */
        private fun getClassName(clazz: Class<*>): String {
            var canonicalName = clazz.canonicalName
            //        canonicalName = canonicalName.substring( HANDLER_PACKAGE_BASE.length() + 1 ).replace( "Handler", "" );
            val begin = canonicalName.lastIndexOf(".") + 1
            canonicalName = canonicalName.substring(begin, canonicalName.length).replace("Handler", "")
            return "/api/" + canonicalName.toLowerCase()
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println(CustomJwt.AUTH_MAP)
        }

        suspend fun initRole2PermissionsMap(dbClient: SQLClient) {
            val dataProvider = LoginDataProvider(dbClient)
            val queryRolesPermission = dataProvider.queryRolesPermission()
            queryRolesPermission.rows.map { rolesPermissionsMap.put(it.getString("role"), getSetFromStr(it.getString("perm"))) }
            println(rolesPermissionsMap)


        }
    }
}
