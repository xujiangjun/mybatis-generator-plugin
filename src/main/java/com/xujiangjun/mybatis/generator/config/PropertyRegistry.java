package com.xujiangjun.mybatis.generator.config;

/**
 * 属性注册中心
 *
 * @author xujiangjun
 * @date 2017-12-04 15:02
 */
public class PropertyRegistry {

    /**
     * 新生成的xml文件是否采用追加模式:true - 追加; false - 覆盖
     */
    public static final String MERGEABLE = "mergeable";

    /**
     * 系统属性中的行分隔符
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * 通过配置文件方式配置作者
     */
    public static final String AUTHOR = "author";

    /**
     * 通过配置文件方式配置是否忽略列类型
     */
    public static final String SUPPRESS_COLUMN_TYPE = "suppressColumnType";

    /**
     * 是否添加Api注解
     */
    public static final String ADD_API_ANNOTATION = "addApiAnnotation";
}
