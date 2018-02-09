package com.bbz.outsource.uaes.oa.http.handlers.auth

import com.bbz.outsource.uaes.oa.consts.ErrorCode
import com.bbz.outsource.uaes.oa.db.LoginDataProvider
import com.bbz.outsource.uaes.oa.http.handlers.auth.anno.RequirePermissions
import com.bbz.outsource.uaes.oa.http.handlers.auth.anno.RequireRoles
import com.bbz.outsource.uaes.oa.http.handlers.endFail
import com.google.common.reflect.ClassPath
import io.vertx.core.Handler
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.sql.SQLClient
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*


class CustomJwtImpl(private val authProvider: JWTAuth) : Handler<RoutingContext> {

    private fun parseAuthHeader(headers: MultiMap): String? {
        val authorization = headers.get(HttpHeaders.AUTHORIZATION) ?: return null
        val parts = authorization.split(" ")
        if (parts.size == 2) {
            val scheme = parts[0]
            val credentials = parts[1]

            if (BEARER == scheme) {
                return credentials
            }
        }

        return null
    }

    override fun handle(context: RoutingContext) {
        val request = context.request()
        val token = parseAuthHeader(request.headers())
        if (token == null) {
            context.response().endFail(ErrorCode.USER_NOT_LOGIN, "No Authorization header was found")
            return
        }

        val authInfo = JsonObject().put("jwt", token)

        authProvider.authenticate(authInfo)
        { res ->
            if (res.succeeded()) {
                val user = res.result()
                val authorise = authorise(user, context)
                if (authorise) {
                    context.setUser(user)
                    val session = context.session()
                    session?.regenerateId()
                    context.next()
                }else{
                    context.response().endFail(ErrorCode.USER_PERMISSION_DENY)
                }

            } else {
                log.warn("JWT decode failure", res.cause())
                context.response().endFail(ErrorCode.USER_NOT_LOGIN, "JWT decode failure")
            }
        }
    }


    private fun authorise(user: User, ctx: RoutingContext): Boolean {
        //    log.debug("检测权限,用户的权限：" + user.principal().getJsonArray("roles"));
        val uri = ctx.request().uri()
        log.debug("访问的地址：" + uri)
        log.debug("需要的权限：" + URI_PERMISSIONS_MAP[uri])
        return doIsPermitted(user.principal().getString("roles"), URI_PERMISSIONS_MAP[uri])
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
            val permisstions = ROLE_PERMISSIONS_MAP[role] ?: return false
            for (permisstion in permisstions) {
                if (uriPermissionSet.contains(permisstion)) {
                    return true
                }
            }
        }
        return false

    }

    companion object {

        private val log = LoggerFactory.getLogger(CustomJwtImpl::class.java)
        private const val HANDLER_PACKAGE_BASE = "com.bbz.outsource.uaes.oa.kt.http.handlers"
        /**
         * 仅供内部使用，原则上初始化之后不允许修改，否则可能造成多线程竞争，如果需要修改，可考虑采用vertx.sharedData()
         * 通过uri获取访问此uri所需要的权限列表
         */
        val URI_PERMISSIONS_MAP = HashMap<String, Set<String>>()

        /**
         * 通过角色获取该角色拥有的权限列表
         */
        private val ROLE_PERMISSIONS_MAP = HashMap<String, Set<String>>()
        /**
         * private static final Pattern BEARER = Pattern.compile( "^Bearer$", Pattern.CASE_INSENSITIVE );         *
         */
        private const val BEARER = "Bearer"

        init {
            try {
                val classpath = ClassPath.from(Thread.currentThread().contextClassLoader)
                for (classInfo in classpath.getTopLevelClassesRecursive(HANDLER_PACKAGE_BASE)) {
                    parseClass(classInfo.load())
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            log.info(URI_PERMISSIONS_MAP.toString())

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
                    URI_PERMISSIONS_MAP[url] = permisstionSet
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
            println(URI_PERMISSIONS_MAP)
        }

        suspend fun initRole2PermissionsMap(dbClient: SQLClient) {
            val dataProvider = LoginDataProvider(dbClient)
            val queryRolesPermission = dataProvider.queryRolesPermission()
            queryRolesPermission.rows.map { ROLE_PERMISSIONS_MAP.put(it.getString("role"), getSetFromStr(it.getString("perm"))) }
            println(ROLE_PERMISSIONS_MAP)
        }
    }
}
