package com.xujiangjun.mybatis.generator.resolver;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.types.JavaTypeResolverDefaultImpl;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * 主要用于将数据库中的类型转换成相应实体类的类型
 *
 * @author xujiangjun
 * @date 2017-12-04 16:53
 */
public class CustomJavaTypeResolver extends JavaTypeResolverDefaultImpl {

    protected Map<String, Integer> typeExtMap;

    public CustomJavaTypeResolver() {
        super();
        typeExtMap = new HashMap<>();
        initTypeSet();
    }

    @Override
    public String calculateJdbcTypeName(IntrospectedColumn introspectedColumn) {
        for (String jdbcType : typeExtMap.keySet()) {
            // 读取自定义配置，value值必须为className，并覆盖默认typeMap
            String value = properties.getProperty(jdbcType);
            if (StringUtility.stringHasValue(value)) {
                typeMap.put(typeExtMap.get(jdbcType), new JdbcTypeInformation(
                        jdbcType.substring(jdbcType.indexOf(".") + 1), new FullyQualifiedJavaType(value)));
            }
        }
        return super.calculateJdbcTypeName(introspectedColumn);
    }

    private void initTypeSet(){
        typeExtMap.put("jdbcType.ARRAY", Types.ARRAY);
        typeExtMap.put("jdbcType.BIGINT", Types.BIGINT);
        typeExtMap.put("jdbcType.BINARY", Types.BINARY);
        typeExtMap.put("jdbcType.BIT", Types.BIT);
        typeExtMap.put("jdbcType.BLOB", Types.BLOB);
        typeExtMap.put("jdbcType.BOOLEAN", Types.BOOLEAN);
        typeExtMap.put("jdbcType.CHAR", Types.CHAR);
        typeExtMap.put("jdbcType.CLOB", Types.CLOB);
        typeExtMap.put("jdbcType.DATALINK", Types.DATALINK);
        typeExtMap.put("jdbcType.DATE", Types.DATE);
        typeExtMap.put("jdbcType.DECIMAL", Types.DECIMAL);
        typeExtMap.put("jdbcType.DISTINCT", Types.DISTINCT);
        typeExtMap.put("jdbcType.DOUBLE", Types.DOUBLE);
        typeExtMap.put("jdbcType.FLOAT", Types.FLOAT);
        typeExtMap.put("jdbcType.INTEGER", Types.INTEGER);
        typeExtMap.put("jdbcType.JAVA_OBJECT", Types.JAVA_OBJECT);
        typeExtMap.put("jdbcType.LONGNVARCHAR", Types.LONGNVARCHAR);
        typeExtMap.put("jdbcType.LONGVARBINARY", Types.LONGVARBINARY);
        typeExtMap.put("jdbcType.LONGVARCHAR", Types.LONGVARCHAR);
        typeExtMap.put("jdbcType.NCHAR", Types.NCHAR);
        typeExtMap.put("jdbcType.NCLOB", Types.NCLOB);
        typeExtMap.put("jdbcType.NVARCHAR", Types.NVARCHAR);
        typeExtMap.put("jdbcType.NULL", Types.NULL);
        typeExtMap.put("jdbcType.NUMERIC", Types.NUMERIC);
        typeExtMap.put("jdbcType.OTHER", Types.OTHER);
        typeExtMap.put("jdbcType.REAL", Types.REAL);
        typeExtMap.put("jdbcType.REF", Types.REF);
        typeExtMap.put("jdbcType.SMALLINT", Types.SMALLINT);
        typeExtMap.put("jdbcType.STRUCT", Types.STRUCT);
        typeExtMap.put("jdbcType.TIME", Types.TIME);
        typeExtMap.put("jdbcType.TIMESTAMP", Types.TIMESTAMP);
        typeExtMap.put("jdbcType.TINYINT", Types.TINYINT);
        typeExtMap.put("jdbcType.VARBINARY", Types.VARBINARY);
        typeExtMap.put("jdbcType.VARCHAR", Types.VARCHAR);
    }
}
