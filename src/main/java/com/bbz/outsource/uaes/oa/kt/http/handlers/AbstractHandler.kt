package com.bbz.outsource.uaes.oa.kt.http.handlers

import com.bbz.outsource.uaes.oa.kt.consts.ErrorCode
import com.bbz.outsource.uaes.oa.kt.consts.ErrorCodeException
import io.vertx.core.json.JsonObject

/**
 *
 */
abstract class  AbstractHandler(){
    /**
     * 检测客户端输入参数是否正确，不多也不少
     *
     * @param keys 需要的key
     * @param arguments 客户上传的json
     */
    protected fun checkArgumentsStrict(arguments: JsonObject, vararg keys: String) {
        println()
        if (arguments.size() != keys.size) {
            throw ErrorCodeException(ErrorCode.PARAMETER_ERROR)
        }
        checkArguments(arguments, *keys)
    }

    /**
     * 检测客户端输入参数是否正确，要求keys内的项目不能少，但是其余的输入不做硬性要求
     *
     * @param keys 需要的key
     * @param arguments 客户上传的json
     */
    protected fun checkArguments(arguments: JsonObject, vararg keys: String) {

        for (key in keys) {
            if (!arguments.containsKey(key)) {
                throw ErrorCodeException(ErrorCode.PARAMETER_ERROR, key + " is null")
                break
            }
        }
    }
}