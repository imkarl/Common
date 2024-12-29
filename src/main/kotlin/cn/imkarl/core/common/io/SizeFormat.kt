package cn.imkarl.core.common.io

import java.math.BigDecimal
import java.text.DecimalFormat

/**
 * 字节大小的格式化工具
 */
object SizeFormat {

    /**
     * 格式化double数据，截取小数点后数字
     */
    @JvmStatic
    private fun formatDouble(str: Double, num: Int): Double {
        val b = BigDecimal(str)
        return b.setScale(num, BigDecimal.ROUND_HALF_UP).toDouble()
    }

    fun formatBytes(bytes: Long): String {
        return if (bytes < 1.KBytes) {
            "${bytes}Bytes"
        } else if (bytes < 1.MBytes) {
            "${DecimalFormat("0.##").format(formatDouble(1.0 * bytes / 1.KBytes, 2))}KB"
        } else if (bytes < 1.GBytes) {
            "${DecimalFormat("0.##").format(formatDouble(1.0 * bytes / 1.MBytes, 2))}MB"
        } else {
            "${DecimalFormat("0.##").format(formatDouble(1.0 * bytes / 1.GBytes, 2))}GB"
        }
    }

}
