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
        SOLARIS,
        MACOSX,
        AIX,
        UNKNOWN
    }

    companion object {
        private val platform by lazy {
            when (osType) {
                OSType.WINDOWS -> WindowsPlatform()
                OSType.MACOSX -> MacOSPlatform()
                OSType.LINUX -> LinuxPlatform()
                else -> throw UnknownError("不支持的平台")
            }
        }

        fun get(): Platform {
            return platform
        }

        val osType: OSType?
            get() {
                val osName = System.getProperty("os.name")
                if (osName != null) {
                    if (osName.contains("Windows")) {
                        return OSType.WINDOWS
                    }
                    if (osName.contains("Linux")) {
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


    fun console(level: LogLevel, message: String) {
        if (LogLevel.ERROR == level || LogLevel.WARN == level) {
            System.err.println("[${level.name}] $message")
        } else {
            System.out.println("[${level.name}] $message")
        }
    }

}
