package com.sumkor;

import com.sumkor.entity.Student;
import com.sumkor.plugin.page.PageUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sumkor
 * @since 2021/8/18
 */
public class SelectByParamTest {

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
     * 字符串替换
     * 默认情况下，使用 #{} 参数语法时，MyBatis 会创建 PreparedStatement 参数占位符，并通过占位符安全地设置参数（就像使用 ? 一样）。
     * 这样做更安全，更迅速，通常也是首选做法，不过有时你就是想直接在 SQL 语句中直接插入一个不转义的字符串。 比如 ORDER BY 子句，这时候你可以：
     * ORDER BY ${columnName}
     *
     * https://mybatis.org/mybatis-3/zh/sqlmap-xml.html
     */
    @Test
    public void selectByParam() throws SQLException {
        /**
         * 解析 mybatis-config.xml 文件时，触发解析 SQL 映射文件
         * @see org.apache.ibatis.builder.xml.XMLMapperBuilder#configurationElement(org.apache.ibatis.parsing.XNode)
         * @see org.apache.ibatis.builder.xml.XMLMapperBuilder#buildStatementFromContext(java.util.List)
         *
         * 对于其中一个 <select|insert|update|delete> 标签节点进行解析
         * @see org.apache.ibatis.builder.xml.XMLStatementBuilder#parseStatementNode()
         *
         * 1. 生成 SqlSource
         * @see org.apache.ibatis.scripting.xmltags.XMLLanguageDriver#createSqlSource(org.apache.ibatis.session.Configuration, org.apache.ibatis.parsing.XNode, java.lang.Class)
         * @see org.apache.ibatis.scripting.xmltags.XMLScriptBuilder#parseScriptNode()
         *
         * 1.1 首先判断是否是动态 SQL，可以看到包含 ${} 符号的就是动态的
         * @see org.apache.ibatis.scripting.xmltags.XMLScriptBuilder#parseDynamicTags(org.apache.ibatis.parsing.XNode)
         * @see org.apache.ibatis.scripting.xmltags.TextSqlNode#isDynamic()
         * @see org.apache.ibatis.scripting.xmltags.TextSqlNode#createParser(org.apache.ibatis.parsing.TokenHandler)
         * @see org.apache.ibatis.parsing.GenericTokenParser#parse(java.lang.String)
         *
         * 1.2 对于 id 为 selectByName，判断是静态 SQL，此时 MixedSqlNode 包含三个 StaticTextSqlNode 对象
         * @see org.apache.ibatis.scripting.xmltags.XMLScriptBuilder#parseScriptNode()
         *
         * 生成 RawSqlSource 对象之前，会调用 MixedSqlNode#apply 生成 SQL 字符串
         * @see org.apache.ibatis.scripting.defaults.RawSqlSource#RawSqlSource(org.apache.ibatis.session.Configuration, org.apache.ibatis.scripting.xmltags.SqlNode, java.lang.Class)
         * @see org.apache.ibatis.scripting.defaults.RawSqlSource#getSql(org.apache.ibatis.session.Configuration, org.apache.ibatis.scripting.xmltags.SqlNode)
         * @see org.apache.ibatis.scripting.xmltags.MixedSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
         *
         * 本例中得到的字符串为：select id, name, phone, email, sex, locked, gmt_created, gmt_modified from student where name=#{name, jdbcType=VARCHAR}
         *
         * 生成 RawSqlSource 对象之前，对字符串中的 #{} 符号进行处理，这里会将 #{} 符号表达式解析为 ParameterMapping 对象，并将 #{} 符号表达式替换为 "?" 字符串
         * @see org.apache.ibatis.scripting.defaults.RawSqlSource#RawSqlSource(org.apache.ibatis.session.Configuration, java.lang.String, java.lang.Class)
         * @see org.apache.ibatis.builder.SqlSourceBuilder#parse(java.lang.String, java.lang.Class, java.util.Map)
         * @see org.apache.ibatis.parsing.GenericTokenParser#parse(java.lang.String)
         * @see org.apache.ibatis.builder.SqlSourceBuilder.ParameterMappingTokenHandler#handleToken(java.lang.String)
         *
         * 最后得到的字符串为：select id, name, phone, email, sex, locked, gmt_created, gmt_modified from student where name=?
         *
         * 1.3 对于 id 为 selectBetweenCreatedTime，判断是动态 SQL，此时 MixedSqlNode 包含三个 SqlNode 对象：StaticTextSqlNode、StaticTextSqlNode、TextSqlNode
         * @see org.apache.ibatis.scripting.xmltags.XMLScriptBuilder#parseScriptNode()
         *
         * 直接利用 MixedSqlNode 生成 DynamicSqlSource，不做进一步解析
         * @see org.apache.ibatis.scripting.xmltags.DynamicSqlSource#DynamicSqlSource(org.apache.ibatis.session.Configuration, org.apache.ibatis.scripting.xmltags.SqlNode)
         *
         * 2. 生成 MappedStatement，将上一步生成的 SqlSource 赋值给它
         * @see org.apache.ibatis.builder.MapperBuilderAssistant#addMappedStatement(java.lang.String, org.apache.ibatis.mapping.SqlSource, org.apache.ibatis.mapping.StatementType, org.apache.ibatis.mapping.SqlCommandType, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.Class, java.lang.String, java.lang.Class, org.apache.ibatis.mapping.ResultSetType, boolean, boolean, boolean, org.apache.ibatis.executor.keygen.KeyGenerator, java.lang.String, java.lang.String, java.lang.String, org.apache.ibatis.scripting.LanguageDriver, java.lang.String)
         */
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            Date beginDate = DateUtils.parseDate("2018-08-29 00:00:00", "yyyy-MM-dd HH:mm:ss");
            Date endDate = DateUtils.parseDate("2018-09-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            PageUtil.setPagingParam(1, 2);
            Map<Object, Object> map = new HashMap<>();
            map.put("bTime", beginDate);
            map.put("eTime", endDate);
            map.put("columnName", "gmt_created");
            List<Student> students = sqlSession.selectList("selectBetweenCreatedTime", map);
            /**
             * 由于 selectBetweenCreatedTime 对应的是动态 SQL，说明是在执行过程中才真正生成 SQL 语句
             * @see org.apache.ibatis.session.defaults.DefaultSqlSession#selectList(java.lang.String, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.executor.CachingExecutor#query(org.apache.ibatis.mapping.MappedStatement, java.lang.Object, org.apache.ibatis.session.RowBounds, org.apache.ibatis.session.ResultHandler)
             * @see org.apache.ibatis.mapping.MappedStatement#getBoundSql(java.lang.Object)
             *
             * 这里的 BoundSql boundSql = ms.getBoundSql(parameterObject);
             * 得到的 BoundSql 实例，得到了预编译后的 SQL 语句：
             *
             * select
             *   id, name, phone, email, sex, locked, gmt_created, gmt_modified
             * from student
             * where gmt_created > ? and gmt_created < ?
             * order by gmt_created desc limit 1,2
             *
             * 关注从 MappedStatement 对象中获取 BoundSql 对象的逻辑。
             *
             * 1. 如果是静态 SQL，直接用 SQL 字符串创建 BoundSql 对象
             * @see org.apache.ibatis.builder.StaticSqlSource#getBoundSql(java.lang.Object)
             *
             * 2. 如果是动态 SQL，则需要与入参一起解析
             * @see org.apache.ibatis.scripting.xmltags.DynamicSqlSource#getBoundSql(java.lang.Object)
             *
             * 已知这里的 MixedSqlNode 包含三个 SqlNode 对象：StaticTextSqlNode、StaticTextSqlNode、TextSqlNode，依次进行解析
             * @see org.apache.ibatis.scripting.xmltags.MixedSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
             *
             * 2.1 对于 StaticTextSqlNode，只需要将 SQL 字符串存入 DynamicContext 中即可
             * @see org.apache.ibatis.scripting.xmltags.StaticTextSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
             *
             * 2.2 对于 TextSqlNode，需要解析 ${} 中的变量。注意，在 ${} 中可以写 OGNL 表达式！
             * @see org.apache.ibatis.scripting.xmltags.TextSqlNode#apply(org.apache.ibatis.scripting.xmltags.DynamicContext)
             * @see org.apache.ibatis.parsing.GenericTokenParser#parse(java.lang.String)
             * @see org.apache.ibatis.scripting.xmltags.TextSqlNode.BindingTokenParser#handleToken(java.lang.String)
             *
             * TextSqlNode#apply 方法执行过后，得到的 SQL 字符串如下：
             *
             * select
             *   id, name, phone, email, sex, locked, gmt_created, gmt_modified
             * from student
             * where gmt_created > #{bTime, jdbcType=TIMESTAMP} and gmt_created < #{eTime, jdbcType=TIMESTAMP}
             * order by gmt_created desc
             *
             * 回到
             * @see org.apache.ibatis.scripting.xmltags.DynamicSqlSource#getBoundSql(java.lang.Object)
             * 解析 SQL 字符串中的 #{} 符号表达式
             * @see org.apache.ibatis.builder.SqlSourceBuilder#parse(java.lang.String, java.lang.Class, java.util.Map)
             * 最终得到 SQL 语句为：
             *
             * select
             *   id, name, phone, email, sex, locked, gmt_created, gmt_modified
             * from student
             * where gmt_created > ? and gmt_created < ?
             * order by gmt_created desc
             *
             * 3. 回到 MappedStatement#getBoundSql，从 sqlSource.getBoundSql 之中拿到 BoundSql 对象之后，对 ParameterMapping 进行剩下的处理
             * @see org.apache.ibatis.mapping.MappedStatement#getBoundSql(java.lang.Object)
             */
            System.out.println("students = " + students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
