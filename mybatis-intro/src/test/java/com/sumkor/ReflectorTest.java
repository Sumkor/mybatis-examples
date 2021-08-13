package com.sumkor;

import com.sumkor.entity.Student;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.junit.Test;

/**
 * @author Sumkor
 * @since 2021/8/13
 */
public class ReflectorTest {

    /**
     * 通过 metaClass，获取 Student.getName() 方法的返回值类型
     *
     * MetaClass 可以被用来解析任意 Class 对象的方法和字段，对外提供了：findProperty、hasSetter、hasGetter、getSetterType、getGetterType 等方法判断属性以及对应的 get、set 方法是否存在。
     * 在 MetaClass 内部，使用了 Reflector 和 PropertyTokenizer 实现上述解析功能。其中，Reflector 是对实体类元信息的封装。
     * 在使用 Reflector 时，还允许指定 ReflectorFactory，默认的 DefaultReflectorFactory 允许对 Reflector 进行缓存。
     */
    @Test
    public void metaClass() {
        DefaultReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass metaClass = MetaClass.forClass(Student.class, reflectorFactory);
        Class<?> nameType = metaClass.getGetterType("name");
        System.out.println("nameType = " + nameType);
    }

    /**
     * MetaObject 和 ObjectWrapper 中关于类级别的方法，例如 hasGetter、hasSetter、findProperty 等方法，都是直接调用 MetaClass 的对应方法实现的。
     * MetaObject 关于对象的方法，例如 getValue、setValue 都是调用 ObjectWrapper 的方法实现的。
     */
    @Test
    public void metaObject() {
        Student student = new Student();
        student.setName("小明");

        MetaObject metaObject = MetaObject.forObject(student, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        boolean hasGetter = metaObject.hasGetter("name");
        System.out.println("hasGetter = " + hasGetter);

        Class<?> type = metaObject.getGetterType("name");
        System.out.println("type = " + type);

        Object name = metaObject.getValue("name");
        /**
         * 底层实际调用 method#invoke
         * @see org.apache.ibatis.reflection.wrapper.BeanWrapper#getBeanProperty(org.apache.ibatis.reflection.property.PropertyTokenizer, java.lang.Object)
         */
        System.out.println("name = " + name);

        /**
         * 执行结果：
         *
         * hasGetter = true
         * type = class java.lang.String
         * name = 小明
         */
    }
}
