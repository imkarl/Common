package cn.imkarl.core.common.platform

import cn.imkarl.core.common.log.LogLevel

/**
 * 平台实现相关
 * @author imkarl
 */
internal abstract class Platform internal constructor() {

    enum class OSType {
        WINDOWS,
        LINUX,
        ANDROID,
        SOLARIS,
        MACOSX,
        AIX,
        UNKNOWN
    }

    companion object {
        val instance by lazy {
            when (osType) {
                OSType.WINDOWS -> WindowsPlatform()
                OSType.MACOSX -> MacOSPlatform()
                OSType.LINUX -> LinuxPlatform()
                OSType.ANDROID -> AndroidPlatform()
                else -> throw UnknownError("不支持的平台")
            }
        }

        val osType: OSType?
            get() {
                val osName = System.getProperty("os.name")
                if (osName != null) {
                    if (osName.contains("Windows")) {
                        return OSType.WINDOWS
                    }
                    if (osName.contains("Linux")) {
                        val httpAgent = System.getProperty("http.agent") ?: ""
                        if (httpAgent.contains("Android")
                            && Class.forName("android.os.Build") != null) {
                            return OSType.ANDROID
                        }
                        return OSType.LINUX
                    }
                    if (osName.contains("Solaris") || osName.contains("SunOS")) {
                        return OSType.SOLARIS
                    }
                    if (osName.contains("OS X")) {
                        return OSType.MACOSX
                    }
                    if (osName.contains("AIX")) {
                        return OSType.AIX
                    }
                    // determine another OS here
                }
                return OSType.UNKNOWN
            }
    }


    open fun console(level: LogLevel, tag: String, message: String) {
        val colorAnsi = when (level) {
            LogLevel.VERBOSE -> ConsoleColors.Black.ansi
            LogLevel.INFO -> ConsoleColors.Green.ansi
            LogLevel.DEBUG -> ConsoleColors.Blue.ansi
            LogLevel.WARN -> ConsoleColors.Yellow.ansi
            LogLevel.ERROR -> ConsoleColors.Red.ansi
        }

        println("$colorAnsi[${level.name}] $tag: $message")
    }

}

private enum class ConsoleColors(val ansi: String) {
    Black("\u001b[30m"),
    Red("\u001b[31m"),
    Green("\u001b[32m"),
    Yellow("\u001b[33m"),
    Blue("\u001b[34m"),
    Purple("\u001b[35m"),
    LightBlue("\u001b[36m"),
    Gray("\u001b[37m"),
    Cyan("\u001b[92m")
}
