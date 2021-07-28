package com.sumkor;

import com.sumkor.entity.Student;
import com.sumkor.mapper.StudentMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * @author 黄泽滨 【huangzebin@i72.com】
 * @since 2021/7/28
 */
public class MapperTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void init() {
        try {
            Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            /**
             * @see org.apache.ibatis.session.SqlSessionFactoryBuilder#build(java.io.Reader, java.lang.String, java.util.Properties)
             *
             * 解析 mybatis-config.xml 文件，其中的 mapper 节点会触发解析 SQL XML 文件
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parse()
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#parseConfiguration(org.apache.ibatis.parsing.XNode)
             *
             * 解析 SQL XML 文件，将 mapper 接口类注册至 Configuration 对象中
             * @see org.apache.ibatis.builder.xml.XMLConfigBuilder#mapperElement(org.apache.ibatis.parsing.XNode)
             * @see org.apache.ibatis.session.Configuration#addMapper(java.lang.Class)
             * @see org.apache.ibatis.binding.MapperRegistry#addMapper(java.lang.Class)
             */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * sqlSession.getMapper
     * 关注生成 Mapper 代理的过程
     */
    @Test
    public void selectByPrimaryKey() {
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            StudentMapper studentMapper = sqlSession.getMapper(StudentMapper.class); // 这里每次 get 出来都得到一个新的实例
            /**
             * 虽然从技术层面上来讲，任何映射器实例的最大作用域与请求它们的 SqlSession 相同。但方法作用域才是映射器实例的最合适的作用域。
             * 也就是说，映射器实例应该在调用它们的方法中被获取，使用完毕之后即可丢弃。 映射器实例并不需要被显式地关闭
             *
             * 获取 Mapper 接口的动态代理实例
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#getMapper(Class)
             * @see org.apache.ibatis.session.Configuration#getMapper(Class, SqlSession)
             * @see org.apache.ibatis.binding.MapperRegistry#getMapper(Class, SqlSession)
             *
             * JDK 动态代理
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(SqlSession)
             * @see org.apache.ibatis.binding.MapperProxyFactory#newInstance(org.apache.ibatis.binding.MapperProxy)
             */

            Student student = studentMapper.selectByPrimaryKey(1);
            /**
             * 交给代理类执行
             * @see org.apache.ibatis.binding.MapperProxy#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
             * @see org.apache.ibatis.binding.MapperProxy.PlainMethodInvoker#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[], org.apache.ibatis.session.SqlSession)
             * @see org.apache.ibatis.binding.MapperMethod#execute(org.apache.ibatis.session.SqlSession, java.lang.Object[])
             *
             * 这里是查询操作
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectOne(java.lang.String, java.lang.Object)
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(java.lang.String, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             *
             * 后续流程与 sqlSession.selectList 一致
             */
            System.out.println(student);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
