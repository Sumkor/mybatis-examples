package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
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
        sqlSession01.close();

        /**
         * 解析 @CacheNamespace 注解，或者解析 xml 配置文件时，会触发构建二级缓存对象
         * 这里采用了装饰器模式
         *
         * @see org.apache.ibatis.builder.MapperBuilderAssistant#useNewCache(java.lang.Class, java.lang.Class, java.lang.Long, java.lang.Integer, boolean, boolean, java.util.Properties)
         * @see org.apache.ibatis.mapping.CacheBuilder#build()
         * @see org.apache.ibatis.mapping.CacheBuilder#setStandardDecorators(org.apache.ibatis.cache.Cache)
         */

        /**
         * 使用二级缓存
         *
         * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler, org.apache.ibatis.cache.CacheKey, org.apache.ibatis.mapping.BoundSql)
         */
    }

    /**
     * 缓存配置见 https://mybatis.org/mybatis-3/zh/sqlmap-xml.html
     *
     * 可用的清除策略有：
     *     LRU – 最近最少使用：移除最长时间不被使用的对象。（默认策略）
     *     FIFO – 先进先出：按对象进入缓存的顺序来移除它们。
     *     SOFT – 软引用：基于垃圾回收器状态和软引用规则移除对象。
     *     WEAK – 弱引用：更积极地基于垃圾收集器状态和弱引用规则移除对象。
     *
     * 最顶层的接口
     * @see org.apache.ibatis.cache.Cache
     *
     * 清除策略
     * @see org.apache.ibatis.cache.decorators.LruCache
     * @see org.apache.ibatis.cache.decorators.FifoCache
     *
     * 最底层的实现
     * @see org.apache.ibatis.cache.impl.PerpetualCache
     */

}
