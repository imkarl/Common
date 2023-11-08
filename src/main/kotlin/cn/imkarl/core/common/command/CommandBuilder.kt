package cn.imkarl.core.common.command

import cn.imkarl.core.common.log.LogUtils
import cn.imkarl.core.common.platform.Platform
import kotlinx.coroutines.*
import java.io.BufferedWriter
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 命令构建器
 * @author imkarl
 */
class CommandBuilder(
    val commands: List<String>,
    val showLog: Boolean = false,
    var directory: File? = null,
    val environment: MutableMap<String, String> = mutableMapOf(),
    var writer: (suspend (BufferedWriter) -> Unit)? = null
) {

    private val defaultCommands: List<String> by lazy {
        if (Platform.osType == Platform.OSType.WINDOWS) {
            mutableListOf("CMD", "/C")
        } else {
            mutableListOf("/bin/sh", "-c")
        }
    }

    private val defaultEnvironments: MutableMap<String, String> by lazy {
        mutableMapOf(
            "PATH" to "${System.getProperty("java.home").removeSuffix("/")}/bin"
        )
    }


    private fun ProcessBuilder.putEnvironments(environments: Map<String, String>): ProcessBuilder {
        if (environments.isNotEmpty()) {
            val oldEnvironment = this.environment()
            environments.forEach { (key, value) ->
                oldEnvironment[key] = if (key == "PATH") {
                    oldEnvironment["PATH"] + ":" + value.removePrefix(":").removePrefix(".:")
                } else {
                    value
                }
            }
        }
        return this
    }

    /**
     * 执行命令
     */
    suspend fun start(
        timeout: Long = -1,
        output: (suspend (line: String, isError: Boolean) -> Unit)? = null
    ): ExecResult {
        return if (timeout <= 0) {
            start(output)
        } else {
            withTimeout(timeout) {
                start(output)
            }
        }
    }
    /**
     * 执行命令
     */
    suspend fun start(
        output: (suspend (line: String, isError: Boolean) -> Unit)? = null
    ): ExecResult {
        if (commands.isEmpty()) {
            return ExecResult(exitCode = 126, output = emptyList(), error = emptyList())
        }

        return supervisorScope {
            suspendCancellableCoroutine { continuation ->
                val outputCache = mutableListOf<String>()
                val errorCache = mutableListOf<String>()

                val startTime = System.currentTimeMillis()
                val process = ProcessBuilder(defaultCommands + commands)
                    .directory(directory)
                    .putEnvironments(defaultEnvironments)
                    .putEnvironments(environment)
                    .start()
                if (showLog) {
                    LogUtils.e("start success （${System.currentTimeMillis() - startTime}ms）")
                }

                // 打印日志
                if (showLog) {
                    LogUtils.i("exec commands: ${commands.joinToString("\n")}")
                }

                // 取消协程时，释放资源
                continuation.invokeOnCancellation {
                    if (showLog) {
                        LogUtils.i("invokeOnCancellation （${System.currentTimeMillis() - startTime}ms）")
                    }
                    process.destroy()
                }

                try {
                    // 写入流
                    if (writer != null) {
                        launch(Dispatchers.IO) {
                            process.outputStream.bufferedWriter().use {
                                writer?.invoke(it)
                            }
                        }
                    }
                    // 输出流
                    launch(Dispatchers.IO) {
                        process.inputStream.bufferedReader().use {
                            while (continuation.context.isActive) {
                                try {
                                    val line = it.readLine() ?: break
                                    outputCache.add(line)
                                    output?.invoke(line, false)
                                } catch (throwable: Throwable) {
                                    continuation.cancel()
                                }
                            }
                        }
                    }
                    // 异常流
                    launch(Dispatchers.IO) {
                        process.errorStream.bufferedReader().use {
                            while (continuation.context.isActive) {
                                try {
                                    val line = it.readLine() ?: break
                                    errorCache.add(line)
                                    output?.invoke(line, true)
                                } catch (throwable: Throwable) {
                                    continuation.cancel()
                                }
                            }
                        }
                    }
                } catch (throwable: Throwable) {
                    if (showLog) {
                        LogUtils.e(throwable)
                    }
                    continuation.resumeWithException(throwable)
                }

                if (showLog) {
                    LogUtils.d("start wait （${System.currentTimeMillis() - startTime}ms）")
                }
                // 等待执行结果
                val exitCode = process.waitFor()
                if (showLog) {
                    LogUtils.d("end （${System.currentTimeMillis() - startTime}ms） isActive:${isActive}")
                }
                if (isActive) {
                    continuation.resume(ExecResult(exitCode = exitCode, output = outputCache, error = errorCache))
                } else {
                    continuation.cancel()
                }
            }
        }
    }

}

data class ExecResult(
    val exitCode: Int,
    val output: List<String>,
    val error: List<String>
)
