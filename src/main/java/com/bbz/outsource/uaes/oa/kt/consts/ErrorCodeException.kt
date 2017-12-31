package com.bbz.outsource.uaes.oa.kt.consts

class ErrorCodeException(val errorCode: ErrorCode, message: String?) : RuntimeException(message) {

    constructor(errorCode: ErrorCode) : this(errorCode,null)


}
fun main(args: Array<String>) {
    val errorCodeException = ErrorCodeException(ErrorCode.DB_ERROR, "db error")
    println(errorCodeException.message+":"+ errorCodeException.errorCode)
val errorCodeException1 = ErrorCodeException(ErrorCode.DB_ERROR)
    println(errorCodeException1.message+":"+ errorCodeException1.errorCode)

}