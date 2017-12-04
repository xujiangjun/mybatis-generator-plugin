package com.xujiangjun.mybatis.generator.formatter;

import com.xujiangjun.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.api.dom.DefaultXmlFormatter;
import org.mybatis.generator.api.dom.xml.Document;

/**
 * 格式化xml
 *
 * @author xujiangjun
 * @date 2017-12-04 17:30
 */
public class CustomXmlFormatter extends DefaultXmlFormatter {

    /**
     * 将xml文档中的jdbcType及其属性全部忽略掉
     *
     * @param document xml文档
     * @return
     */
    @Override
    public String getFormattedContent(Document document) {
        String content = document.getFormattedContent();
        boolean suppressColumnType = Boolean.parseBoolean(context.getProperty(PropertyRegistry.SUPPRESS_COLUMN_TYPE));
        if(suppressColumnType){
            String regex = "[, ]?jdbcType=\"?[A-Z]+\"?";
            content = content.replaceAll(regex, "");
        }
        return content;
    }
}
