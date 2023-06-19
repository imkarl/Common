package cn.imkarl.core.common.io.file

import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*

/**
 * Properties加载工具类
 * @author imkarl
 */
object PropertiesLoaderUtils {

    fun loadProperties(
        inputStream: InputStream,
        charset: Charset = Charsets.UTF_8
    ): Properties {
        return Properties().apply {
            this.load(InputStreamReader(inputStream, charset))
        }
    }

    fun loadProperties(
        file: File,
        charset: Charset = Charsets.UTF_8
    ): Properties {
        return loadProperties(file.inputStream(), charset)
    }

}