package com.xujiangjun.mybatis.generator;

import com.xujiangjun.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.DefaultCommentGenerator;
import org.mybatis.generator.internal.util.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * 自定义注释生成器
 *
 * @author xujiangjun
 * @date 2017-12-04 13:14
 */
public class CustomCommentGenerator extends DefaultCommentGenerator{

    protected Properties properties;

    @Override
    public void addConfigurationProperties(Properties properties) {
        super.addConfigurationProperties(properties);
        this.properties = new Properties();
        this.properties.putAll(properties);
    }

    /**
     * 添加Copyright说明
     *
     * @param compilationUnit
     */
    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        compilationUnit.addFileCommentLine("/*");
        compilationUnit.addFileCommentLine(" * ");
        compilationUnit.addFileCommentLine(" * Copyright 2017 xujiangjun.");
        compilationUnit.addFileCommentLine(" * ");
        compilationUnit.addFileCommentLine(" */");
    }

    /**
     * 添加类注释，并让类实现Serializable接口
     *
     * @param topLevelClass 实体类
     * @param introspectedTable 数据库表
     */
    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addJavaDocLine("/**");
        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            topLevelClass.addJavaDocLine(" * ");
            String[] remarkLines = remarks.split(System.getProperty(PropertyRegistry.LINE_SEPARATOR));
            for (String remarkLine : remarkLines) {
                topLevelClass.addJavaDocLine(" * " + remarkLine);
            }
        }
        topLevelClass.addJavaDocLine(" * ");
        // 1.获取表名并添加到JavaDoc中
        StringBuilder sb = new StringBuilder();
        sb.append(" * ").append(introspectedTable.getFullyQualifiedTable()).append(" 表对应的实体类");
        topLevelClass.addJavaDocLine(sb.toString());
        topLevelClass.addJavaDocLine(" * ");
        // 2.添加作者和日期信息到JavaDoc中
        String author = properties.getProperty(PropertyRegistry.AUTHOR);
        if (author == null) {
            author = "$author";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        topLevelClass.addJavaDocLine(" * @author \t" + author);
        topLevelClass.addJavaDocLine(" * @date \t" + sdf.format(new Date()));
        topLevelClass.addJavaDocLine(" */");
        // 3.实现Serializable接口
        implementsSerializable(topLevelClass);
    }

    /**
     * 添加字段注释
     * 通过introspectedColumn.getActualColumnName()可以获取对应的数据库列名
     *
     * @param field 字段列
     * @param introspectedTable  数据库表
     * @param introspectedColumn 数据库列
     */
    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            sb.append("/** ").append(remarks).append(" **/");
        }
        field.addJavaDocLine(sb.toString());
    }

    /**
     * 实体类继承Serializable接口
     * 1. new JavaType
     * 2. import java.io.Serializable
     * 3. implements Serializable
     *
     * @param topLevelClass 实体类
     */
    private void implementsSerializable(TopLevelClass topLevelClass) {
        FullyQualifiedJavaType serializable = new FullyQualifiedJavaType("java.io.Serializable");
        topLevelClass.addImportedType(serializable);
        topLevelClass.addSuperInterface(serializable);
        Field serialVersionUID = new Field();
        serialVersionUID.setVisibility(JavaVisibility.PRIVATE);
        serialVersionUID.setStatic(true);
        serialVersionUID.setFinal(true);
        serialVersionUID.setName("serialVersionUID");
        serialVersionUID.setType(new FullyQualifiedJavaType("long"));
        serialVersionUID.setInitializationString("1L");
        serialVersionUID.addJavaDocLine("");
        topLevelClass.addField(serialVersionUID);
    }
}
