package cn.imkarl.core.common.http

import io.ktor.http.HttpMethod

val HttpMethod.Companion.NO_IMPL: HttpMethod get() = HttpMethod("*NO*IMPL*")

val HttpMethod.Companion.COPY: HttpMethod get() = HttpMethod("COPY")
val HttpMethod.Companion.LOCK: HttpMethod get() = HttpMethod("LOCK")
val HttpMethod.Companion.UNLOCK: HttpMethod get() = HttpMethod("UNLOCK")
val HttpMethod.Companion.MOVE: HttpMethod get() = HttpMethod("MOVE")
val HttpMethod.Companion.MKCOL: HttpMethod get() = HttpMethod("MKCOL")
val HttpMethod.Companion.PROPFIND: HttpMethod get() = HttpMethod("PROPFIND")
val HttpMethod.Companion.PROPPATCH: HttpMethod get() = HttpMethod("PROPPATCH")
