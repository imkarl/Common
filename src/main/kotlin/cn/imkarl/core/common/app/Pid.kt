package cn.imkarl.core.common.app

import java.lang.management.ManagementFactory

/**
 * 进程ID单例封装<br></br>
 * 第一次访问时调用[ManagementFactory.getRuntimeMXBean]获取PID信息，之后直接使用缓存值
 */
object Pid {

    private val pid: Int

    init {
        this.pid = getPid()
    }

    /**
     * 获取PID值
     *
     * @return pid
     */
    fun get(): Int {
        return this.pid
    }

    /**
     * 获取当前进程ID，首先获取进程名称，读取@前的ID值，如果不存在，则读取进程名的hash值
     *
     * @return 进程ID
     * @throws UnsupportedOperationException 进程名称为空
     */
    private fun getPid(): Int {
        val processName = ManagementFactory.getRuntimeMXBean().name
        if (processName.isBlank()) {
            throw UnsupportedOperationException("Process name is blank!")
        }
        val atIndex = processName.indexOf('@')
        return if (atIndex > 0) {
            processName.substring(0, atIndex).toInt()
        } else {
            processName.hashCode()
        }
    }

}