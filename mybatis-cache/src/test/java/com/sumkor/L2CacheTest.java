package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.TransactionalCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 当开启缓存后，数据的查询执行的流程就是 二级缓存 -> 一级缓存 -> 数据库。
 * 二级缓存开启后，同一个 namespace 下的所有操作语句，都影响着同一个 Cache，即二级缓存被多个 SqlSession 共享，是一个全局的变量。
 * MyBatis 的二级缓存相对于一级缓存来说，实现了 SqlSession 之间缓存数据的共享，同时粒度更加的细，能够到 namespace 级别，通过 Cache 接口实现类不同的组合，对 Cache 的可控性也更强。
 *
 * @author Sumkor
 * @since 2021/8/11
 */
public class L2CacheTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不同 SqlSession，可以共同使用二级缓存
     */
    @Test
    public void cache() {
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        List<Student> students01 = sqlSession01.selectList("selectAll");
        System.out.println("\r\n students01 = " + students01 + "\r\n");
        sqlSession01.close();

        SqlSession sqlSession02 = sqlSessionFactory.openSession();
        List<Student> students02 = sqlSession02.selectList("selectAll");
        System.out.println("\r\n students02 = " + students02 + "\r\n");
        sqlSession02.close();

        /**
         * 1. 构建二级缓存对象
         *
         * 解析 @CacheNamespace 注解，或者解析 xml 配置文件时，会触发构建二级缓存对象
         * @see org.apache.ibatis.builder.xml.XMLMapperBuilder#cacheElement(org.apache.ibatis.parsing.XNode)
         *
         * 构建二级缓存对象，这里采用了装饰器模式
         * @see org.apache.ibatis.builder.MapperBuilderAssistant#useNewCache(java.lang.Class, java.lang.Class, java.lang.Long, java.lang.Integer, boolean, boolean, java.util.Properties)
         * @see org.apache.ibatis.mapping.CacheBuilder#build()
         * @see org.apache.ibatis.mapping.CacheBuilder#setStandardDecorators(org.apache.ibatis.cache.Cache)
         *
         * 2. 使用二级缓存！！！
         *
         * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
         */
    }

    /**
     * sqlSession01 未提交事务之前，sqlSession02 无法从二级缓存中拿到数据，避免了脏读
     */
    @Test
    public void cacheTx() {
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        List<Student> students01 = sqlSession01.selectList("selectAll");
        System.out.println("\r\n students01 = " + students01 + "\r\n");

        SqlSession sqlSession02 = sqlSessionFactory.openSession();
        List<Student> students02 = sqlSession02.selectList("selectAll");
        System.out.println("\r\n students02 = " + students02 + "\r\n");

        sqlSession01.close();
        sqlSession02.close();
    }

    /**
     * 缓存配置
     * 见 https://mybatis.org/mybatis-3/zh/sqlmap-xml.html
     *
     * 可用的清除策略有：
     *     LRU – 最近最少使用：移除最长时间不被使用的对象。（默认策略）
     *     FIFO – 先进先出：按对象进入缓存的顺序来移除它们。
     *     SOFT – 软引用：基于垃圾回收器状态和软引用规则移除对象。（软引用：内存溢出之前会回收）
     *     WEAK – 弱引用：更积极地基于垃圾收集器状态和弱引用规则移除对象。（弱引用：每次 GC 时都会回收）
     *
     * 最顶层的接口
     * @see org.apache.ibatis.cache.Cache
     *
     * 清除策略
     * @see org.apache.ibatis.cache.decorators.LruCache
     * @see org.apache.ibatis.cache.decorators.FifoCache
     * @see org.apache.ibatis.cache.decorators.SoftCache
     * @see org.apache.ibatis.cache.decorators.WeakCache
     *
     * 最底层的实现
     * @see org.apache.ibatis.cache.impl.PerpetualCache
     *
     * 说明：
     * SynchronizedCache： 同步 Cache，实现比较简单，直接使用 synchronized 修饰方法。
     * LoggingCache： 日志功能，装饰类，用于记录缓存的命中率，如果开启了DEBUG模式，则会输出命中率日志。
     * SerializedCache： 序列化功能，将值序列化后存到缓存中。该功能用于缓存返回一份实例的 Copy，用于保存线程安全。
     * LruCache： 采用了 Lru 算法的 Cache 实现，移除最近最少使用的 key/value。
     * PerpetualCache： 作为为最基础的缓存类，底层实现比较简单，直接使用了 HashMap。
     *
     *
     * 假设配置了二级缓存： <cache eviction="FIFO" flushInterval="60000" size="512" readOnly="false"/>
     * 这里得到的缓存对象： SynchronizedCache -> LoggingCache -> SerializedCache -> ScheduledCache -> FifoCache -> PerpetualCache
     *
     * type： cache 使用的类型，默认是 PerpetualCache，是对 Cache 接口最基本的实现。
     * eviction： 定义回收的策略，常见的有 FIFO，LRU。
     * flushInterval： 配置一定时间自动刷新缓存，单位是毫秒。
     * size： 最多缓存对象的个数。
     * readOnly： 是否只读，若配置可读写，则需要对应的实体类能够序列化。默认值是 false。
     * blocking： 若缓存中找不到对应的 key，是否会一直 blocking，直到有对应的数据进入缓存。默认值是 false。
     *
     *
     * 关于 readOnly
     *
     * 缓存配置 readOnly="false" 时，会使用 SerializedCache，也就是从缓存中读到的对象是反序列化而来的，因此是不同的对象实例。（本例中 students01 和 students02 是不同实例）
     * 缓存配置 readOnly="true" 时，不会使用 SerializedCache。（本例中 students01 和 students02 是相同实例）
     * @see org.apache.ibatis.cache.decorators.SerializedCache
     *
     * 这个特性为什么要用 readOnly 来标识呢？很反直觉。。
     * 应该只是一个提醒，readOnly = true 的时候，要求开发者不要修改返回的缓存对象实例，以免对其他使用该对象的线程造成影响
     * Mybatis 本身并没有在 readOnly = true 时，做任何禁止修改缓存对象实例的限制
     *
     * 官方的说明：
     * 只读的缓存会给所有调用者返回缓存对象的相同实例。 因此这些对象不能被修改。这就提供了可观的性能提升。
     * 而可读写的缓存会（通过序列化）返回缓存对象的拷贝。 速度上会慢一些，但是更安全，因此默认值是 false。
     *
     * 关于 blocking
     *
     * @see org.apache.ibatis.cache.decorators.BlockingCache
     */

    /**
     * 二级缓存 vs 多表查询
     *
     * MyBatis 的二级缓存不适应用于映射文件中存在多表查询的情况。
     * 通常我们会为每个单表创建单独的映射文件，由于 MyBatis 的二级缓存是基于 namespace 的，
     * 多表查询语句所在的 namespace 无法感应到其他 namespace 中的语句对多表查询中涉及的表进行的修改，引发脏数据问题。
     *
     * 为了解决这个问题，可以在 mapper.xml 中使用 Cache ref 标签，使得多个 namespace 使用同一个 Cache 对象。
     * 不过这样做的后果是，缓存的粒度变粗了，多个 Mapper namespace 下的所有操作都会对缓存使用造成影响。
     *
     * MyBatis 在多表查询时，极大可能会出现脏数据，有设计上的缺陷，安全使用二级缓存的条件比较苛刻。
     * 在分布式环境下，由于默认的 MyBatis Cache 实现都是基于本地的，分布式环境下必然会出现读取到脏数据，
     * 需要使用集中式缓存将 MyBatis 的 Cache 接口实现，有一定的开发成本，直接使用 Redis，Memcached 等分布式缓存可能成本更低，安全性也更高。
     */

    /**
     * TransactionalCache::new 的测试
     */
    @Test
    public void transactionalCache() {
        HashMap<Cache, TransactionalCache> hashMap = new HashMap<>();
        hashMap.computeIfAbsent(new PerpetualCache("id"), TransactionalCache::new);
        System.out.println("hashMap = " + hashMap);
    }

    @Test
    public void blockingCache() {
        ConcurrentHashMap<Integer, String> map = new ConcurrentHashMap<>();
        String value01 = map.putIfAbsent(1, "a");
        System.out.println("value01 = " + value01); // null
        System.out.println("map = " + map); // {1=a}

        String value02 = map.putIfAbsent(1, "b");
        System.out.println("value02 = " + value02); // a
        System.out.println("map = " + map); // {1=a}
    }
}
