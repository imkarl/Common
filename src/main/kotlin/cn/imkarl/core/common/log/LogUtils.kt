package cn.imkarl.core.common.log

import cn.imkarl.core.common.app.AppUtils
import cn.imkarl.core.common.platform.Platform
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException

/**
 * 日志相关工具类
 * @author imkarl
 */
object LogUtils {

    private var _maxLineLength = 3000
    private var _globalTag = AppUtils.appName


    /**
     * 设置最大打印长度
     */
    @JvmStatic
    fun setMaxLength(length: Int) {
        this._maxLineLength = length
    }

    /**
     * 设置全局TAG
     */
    @JvmStatic
    fun setGlobalTag(tag: String) {
        this._globalTag = tag
    }



    @JvmStatic
    fun v(message: Any?) {
        log(LogLevel.VERBOSE, _globalTag, message)
    }
    @JvmStatic
    fun v(tag: String, message: Any?) {
        log(LogLevel.VERBOSE, tag, message)
    }

    @JvmStatic
    fun d(message: Any?) {
        log(LogLevel.DEBUG, _globalTag, message)
    }
    @JvmStatic
    fun d(tag: String, message: Any?) {
        log(LogLevel.DEBUG, tag, message)
    }

    @JvmStatic
    fun i(message: Any?) {
        log(LogLevel.INFO, _globalTag, message)
    }
    @JvmStatic
    fun i(tag: String, message: Any?) {
        log(LogLevel.INFO, tag, message)
    }

    @JvmStatic
    fun w(message: Any?) {
        log(LogLevel.WARN, _globalTag, message)
    }
    @JvmStatic
    fun w(tag: String, message: Any?) {
        log(LogLevel.WARN, tag, message)
    }

    @JvmStatic
    fun e(message: Any?) {
        log(LogLevel.ERROR, _globalTag, message)
    }
    @JvmStatic
    fun e(tag: String, message: Any?) {
        log(LogLevel.ERROR, tag, message)
    }


    @JvmStatic
    fun println(level: LogLevel, msg: String?) {
        console(level, _globalTag, toString(msg))
    }
    @JvmStatic
    fun println(level: LogLevel, tag: String, msg: String?) {
        console(level, tag, toString(msg))
    }


    private fun log(level: LogLevel, tag: String, message: Any?) {
        val element = Throwable().stackTrace[2]
        var className = element.className
        className = className.substring(className.lastIndexOf(".") + 1)
        val codeLine = className + '.' + element.methodName + '(' + element.fileName + ':' + element.lineNumber + ')'
        console(level, tag, codeLine + "\n\t" + toString(message))
    }

    /**
     * 真实的打印方法
     */
    private fun console(level: LogLevel, tag: String, msg: String) {
        if (msg.length <= _maxLineLength) {
            console(level, "$tag: $msg")
            return
        }

        // 超出目标长度，自动换行打印
        var startIndex = 0
        var endIndex = _maxLineLength
        while (endIndex > startIndex) {
            console(level, "$tag: ${(if (startIndex == 0) "" else "\t↑↑↑↑\n") + msg.substring(startIndex, endIndex)}")

            startIndex = endIndex
            endIndex = Math.min(msg.length, startIndex + _maxLineLength)
        }
    }
    private fun console(level: LogLevel, message: String) {
        Platform.get().console(level, message)
    }

    private fun toString(message: Any?): String {
        if (message == null) {
            return "[NULL]"
        }
        if (message is Throwable) {
            var t = message as Throwable?
            while (t != null) {
                if (t is UnknownHostException) {
                    return t.toString()
                }
                t = t.cause
            }

            val sw = StringWriter()
            val pw = PrintWriter(sw, false)
            message.printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }
        return message.toString()
    }

}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR
}
