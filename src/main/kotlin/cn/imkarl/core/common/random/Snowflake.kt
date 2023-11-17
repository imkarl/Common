package cn.imkarl.core.common.random

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

class Snowflake(

    /**
     * 机器id
     */
    private val workerId: Long = 0,
    /**
     * 数据中心id
     */
    private val dataCenterId: Long = 0,

    /**
     * 当在低频模式下时，序号始终为0，导致生成ID始终为偶数<br></br>
     * 此属性用于限定一个随机上限，在不同毫秒下生成序号时，给定一个随机数，避免偶数问题。<br></br>
     * 注意次数必须小于[.SEQUENCE_MASK]，`0`表示不使用随机数。<br></br>
     * 这个上限不包括值本身。
     */
    private val randomSequenceLimit: Long = 0,


    /**
     * 允许的时钟回拨毫秒数
     */
    private val timeOffset: Long = DEFAULT_TIME_OFFSET,

    /**
     * 初始化时间点
     */
    private val twepoch: Long = DEFAULT_TWEPOCH,
) {

    companion object {

        private val POOL = ConcurrentHashMap<String, Snowflake>()


        /**
         * 默认的起始时间，为Thu, 04 Nov 2010 01:42:54 GMT
         */
        internal var DEFAULT_TWEPOCH = 1288834974657L

        /**
         * 默认回拨时间，2S
         */
        internal var DEFAULT_TIME_OFFSET = 2000L

        private const val WORKER_ID_BITS = 5L

        // 最大支持机器节点数0~31，一共32个
        private const val MAX_WORKER_ID = -1L xor (-1L shl WORKER_ID_BITS.toInt())
        private const val DATA_CENTER_ID_BITS = 5L

        // 最大支持数据中心节点数0~31，一共32个
        private const val MAX_DATA_CENTER_ID = -1L xor (-1L shl DATA_CENTER_ID_BITS.toInt())

        // 序列号12位（表示只允许workId的范围为：0-4095）
        private const val SEQUENCE_BITS = 12L

        // 机器节点左移12位
        private const val WORKER_ID_SHIFT = SEQUENCE_BITS

        // 数据中心节点左移17位
        private const val DATA_CENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS

        // 时间毫秒数左移22位
        private const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_ID_BITS

        // 序列掩码，用于限定序列最大值不能超过4095
        private const val SEQUENCE_MASK = (-1L shl SEQUENCE_BITS.toInt()).inv() // 4095


        /**
         * 获取单例的Twitter的Snowflake 算法生成器对象<br></br>
         * 分布式系统中，有一些需要使用全局唯一ID的场景，有些时候我们希望能使用一种简单一些的ID，并且希望ID能够按照时间有序生成。
         *
         * snowflake的结构如下(每部分用-分开):<br></br>
         *
         * <pre>
         * 0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
         * </pre>
         *
         * 第一位为未使用，接下来的41位为毫秒级时间(41位的长度可以使用69年)<br></br>
         * 然后是5位datacenterId和5位workerId(10位的长度最多支持部署1024个节点）<br></br>
         * 最后12位是毫秒内的计数（12位的计数顺序号支持每个节点每毫秒产生4096个ID序号）
         *
         * 参考：http://www.cnblogs.com/relucent/p/4955340.html
         *
         * @param workerId     终端ID
         * @param datacenterId 数据中心ID
         * @return [Snowflake]
         */
        fun getSnowflake(workerId: Long, datacenterId: Long): Snowflake {
            val key = buildKey(this::class.java.name, workerId, datacenterId)
            return POOL.getOrPut(key) { Snowflake(workerId, datacenterId) }
        }

        // ------------------------------------------------------------------------------------------- Private method start
        /**
         * 构建key
         *
         * @param className 类名
         * @param params    参数列表
         * @return key
         */
        private fun buildKey(className: String, vararg params: Any): String {
            return if (params.isEmpty()) {
                className
            } else {
                "${className}#${params.joinToString("_")}"
            }
        }

    }

    init {
        checkBetween(workerId, 0, MAX_WORKER_ID)
        checkBetween(dataCenterId, 0, MAX_DATA_CENTER_ID)
        checkBetween(randomSequenceLimit, 0, SEQUENCE_MASK)
    }


    /**
     * 自增序号，当高频模式下时，同一毫秒内生成N个ID，则这个序号在同一毫秒下，自增以避免ID重复。
     */
    private var sequence = 0L
    private var lastTimestamp = -1L

    /**
     * 根据Snowflake的ID，获取机器id
     *
     * @param id snowflake算法生成的id
     * @return 所属机器的id
     */
    fun getWorkerId(id: Long): Long {
        return id shr WORKER_ID_SHIFT.toInt() and (-1L shl WORKER_ID_BITS.toInt()).inv()
    }

    /**
     * 根据Snowflake的ID，获取数据中心id
     *
     * @param id snowflake算法生成的id
     * @return 所属数据中心
     */
    fun getDataCenterId(id: Long): Long {
        return id shr DATA_CENTER_ID_SHIFT.toInt() and (-1L shl DATA_CENTER_ID_BITS.toInt()).inv()
    }

    /**
     * 根据Snowflake的ID，获取生成时间
     *
     * @param id snowflake算法生成的id
     * @return 生成的时间
     */
    fun getGenerateDateTime(id: Long): Long {
        return (id shr TIMESTAMP_LEFT_SHIFT.toInt() and (-1L shl 41L.toInt()).inv()) + twepoch
    }

    /**
     * 下一个ID
     *
     * @return ID
     */
    @Synchronized
    fun nextId(): Long {
        var timestamp: Long = genTime()
        if (timestamp < lastTimestamp) {
            if (lastTimestamp - timestamp < timeOffset) {
                // 容忍指定的回拨，避免NTP校时造成的异常
                timestamp = lastTimestamp
            } else {
                // 如果服务器时间有问题(时钟后退) 报错。
                throw IllegalStateException(
                    "Clock moved backwards. Refusing to generate id for ${lastTimestamp - timestamp}ms",
                )
            }
        }
        if (timestamp == lastTimestamp) {
            val sequence = sequence + 1 and SEQUENCE_MASK
            if (sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
            this.sequence = sequence
        } else {
            // issue#I51EJY
            sequence = if (randomSequenceLimit > 1) {
                ThreadLocalRandom.current().nextLong(randomSequenceLimit)
            } else {
                0L
            }
        }
        lastTimestamp = timestamp
        return (timestamp - twepoch shl TIMESTAMP_LEFT_SHIFT.toInt()
                or (dataCenterId shl DATA_CENTER_ID_SHIFT.toInt())
                or (workerId shl WORKER_ID_SHIFT.toInt())
                or sequence)
    }

    // ------------------------------------------------------------------------------------------------------------------------------------ Private method start
    /**
     * 循环等待下一个时间
     *
     * @param lastTimestamp 上次记录的时间
     * @return 下一个时间
     */
    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = genTime()
        // 循环直到操作系统时间戳变化
        while (timestamp == lastTimestamp) {
            timestamp = genTime()
        }
        if (timestamp < lastTimestamp) {
            // 如果发现新的时间戳比上次记录的时间戳数值小，说明操作系统时间发生了倒退，报错
            throw java.lang.IllegalStateException(
                "Clock moved backwards. Refusing to generate id for ${lastTimestamp - timestamp}ms"
            )
        }
        return timestamp
    }

    /**
     * 生成时间戳
     *
     * @return 时间戳
     */
    private fun genTime(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 检查值是否在指定范围内
     *
     * @param value            值
     * @param min              最小值（包含）
     * @param max              最大值（包含）
     * @return 经过检查后的值
     */
    private fun checkBetween(value: Long, min: Long, max: Long) {
        if (value < min || value > max) {
            throw IllegalArgumentException("The value must be between $min and ${max}.")
        }
    }

}