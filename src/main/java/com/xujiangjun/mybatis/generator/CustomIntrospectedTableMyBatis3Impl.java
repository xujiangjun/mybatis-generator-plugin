package com.xujiangjun.mybatis.generator;

import com.xujiangjun.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.api.GeneratedXmlFile;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xujiangjun
 * @date 2017-12-04 14:42
 */
public class CustomIntrospectedTableMyBatis3Impl extends IntrospectedTableMyBatis3Impl {

    @Override
    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        List<GeneratedXmlFile> answer = new ArrayList<>();
        if (xmlMapperGenerator != null) {
            // 新生成的xml文件是否采用追加模式:true - 追加; false - 覆盖
            boolean isMergeable = false;
            if (Boolean.parseBoolean(context.getProperty(PropertyRegistry.MERGEABLE))) {
                isMergeable = true;
            }
            Document document = xmlMapperGenerator.getDocument();
            GeneratedXmlFile gxf = new GeneratedXmlFile(document, getMyBatis3XmlMapperFileName(),
                    getMyBatis3XmlMapperPackage(), context.getSqlMapGeneratorConfiguration().getTargetProject(),
                    isMergeable, context.getXmlFormatter());
            if (context.getPlugins().sqlMapGenerated(gxf, this)) {
                answer.add(gxf);
            }
        }
        return answer;
    }
}
