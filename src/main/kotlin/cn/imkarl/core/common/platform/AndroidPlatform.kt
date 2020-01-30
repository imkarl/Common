package cn.imkarl.core.common.platform

import cn.imkarl.core.common.log.LogLevel
import org.joor.Reflect

/**
 * Android平台
 * @author imkarl
 */
internal class AndroidPlatform: LinuxPlatform() {

    override fun console(level: LogLevel, tag: String, message: String) {
        val priority = when (level) {
            LogLevel.VERBOSE -> 2
            LogLevel.INFO -> 4
            LogLevel.DEBUG -> 3
            LogLevel.WARN -> 5
            LogLevel.ERROR -> 6
        }

        Reflect.on("android.util.Log").call("println", priority, tag, message)
    }

}
