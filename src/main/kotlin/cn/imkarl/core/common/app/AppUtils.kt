package cn.imkarl.core.common.app

/**
 * APP相关工具类
 * @author imkarl
 */
object AppUtils {

    @JvmStatic
    lateinit var packageName: String
        private set

    @JvmStatic
    lateinit var appName: String
        private set


    private var _isDebug = false
    @JvmStatic
    val isDebug
        get() = _isDebug

    @JvmField
    val isJarRun = AppUtils::class.java.classLoader.getResource(".") == null

    @JvmStatic
    @JvmOverloads
    fun init(
        packageName: String,
        appName: String,
        isDebug: Boolean = false
    ) {
        this.packageName = packageName
        this.appName = appName
        this._isDebug = isDebug
    }

    @JvmStatic
    fun setDebug(isDebug: Boolean) {
        this._isDebug = isDebug
    }

}
