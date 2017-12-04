package com.xujiangjun.mybatis.generator;

import com.xujiangjun.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.*;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * 为mapper添加注释，修改mapperExt的生成方式
 *
 * @author xujiangjun
 * @date 2017-12-04 17:40
 */
public class CustomGeneratorPlugin extends PluginAdapter {

    private static String XML_FILE_SUFFIX = "Ext";

    private static String JAVA_FILE_SUFFIX = "Ext";

    private static String SQL_MAP_COMMON_SUFFIX = "and is_deleted = 0";

    private static String ANNOTATION_REPOSITORY = "org.springframework.stereotype.Repository";

    private static String ANNOTATION_API_PARAM = "com.xujiangjun.ApiParam";

    private static FullyQualifiedJavaType API_PARAM_INSTANCE = new FullyQualifiedJavaType(ANNOTATION_API_PARAM);

    private static String MAPPER_EXT_HINT = "<!-- 扩展自动生成或自定义的SQl语句写在此文件中 -->";

    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        addMapperClassComment(interfaze, introspectedTable, false);
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field,
                                       TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
                                       IntrospectedTable introspectedTable,
                                       Plugin.ModelClassType modelClassType) {
        boolean addApiAnnotation = Boolean.parseBoolean(context.getProperty("addApiAnnotation"));
        if (addApiAnnotation) {
            if (!topLevelClass.getImportedTypes().contains(API_PARAM_INSTANCE)) {
                topLevelClass.addImportedType(API_PARAM_INSTANCE);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("@ApiParam(description=\"");
            String remarks = introspectedColumn.getRemarks();
            if (StringUtility.stringHasValue(remarks)) {
                sb.append(remarks);
            }
            sb.append("\")");
            field.addAnnotation(sb.toString());
        }
        return true;
    }

    /**
     * 创建xxxMapperExt.java
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(
                introspectedTable.getMyBatis3JavaMapperType() + JAVA_FILE_SUFFIX);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(interfaze);

        FullyQualifiedJavaType baseInterface = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        interfaze.addSuperInterface(baseInterface);

        // 添加注释
        addMapperClassComment(interfaze, introspectedTable, true);

        FullyQualifiedJavaType annotation = new FullyQualifiedJavaType(ANNOTATION_REPOSITORY);
        interfaze.addImportedType(annotation);
        interfaze.addAnnotation("@Repository");

        CompilationUnit compilationUnits = interfaze;
        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(
                compilationUnits,
                context.getJavaModelGeneratorConfiguration().getTargetProject(),
                context.getProperty(org.mybatis.generator.config.PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
                context.getJavaFormatter());

        if (isExistExtFile(generatedJavaFile.getTargetProject(), generatedJavaFile.getTargetPackage(),
                generatedJavaFile.getFileName())) {
            return super.contextGenerateAdditionalJavaFiles(introspectedTable);
        }
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<>(1);
        generatedJavaFile.getFileName();
        generatedJavaFiles.add(generatedJavaFile);
        return generatedJavaFiles;
    }


    /**
     * 创建xxxMapperExt.xml
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        String[] splitFile = introspectedTable.getMyBatis3XmlMapperFileName().split("\\.");
        String fileNameExt = null;
        if (splitFile[0] != null) {
            fileNameExt = splitFile[0] + XML_FILE_SUFFIX + ".xml";
        }

        if (isExistExtFile(context.getSqlMapGeneratorConfiguration().getTargetProject(),
                introspectedTable.getMyBatis3XmlMapperPackage(), fileNameExt)) {
            return super.contextGenerateAdditionalXmlFiles(introspectedTable);
        }

        Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID, XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);

        XmlElement root = new XmlElement("mapper");
        document.setRootElement(root);
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace() + XML_FILE_SUFFIX;
        root.addAttribute(new Attribute("namespace", namespace));
        root.addElement(new TextElement(MAPPER_EXT_HINT));

        GeneratedXmlFile gxf = new GeneratedXmlFile(document, fileNameExt,
                introspectedTable.getMyBatis3XmlMapperPackage(),
                context.getSqlMapGeneratorConfiguration().getTargetProject(),
                false, context.getXmlFormatter());

        List<GeneratedXmlFile> answer = new ArrayList<>(1);
        answer.add(gxf);
        return answer;
    }

    /**
     * 增删改默认mapper.xml中的sql语句及属性
     *
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement parentElement = document.getRootElement();
        updateDocumentNameSpace(introspectedTable, parentElement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 修改gmt_create, modifier, gmt_modified等语句
     *
     * @param element
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
                                                                     IntrospectedTable introspectedTable) {
        List<Element> elements = element.getElements();
        XmlElement setItem = null;
        int modifierItemIndex = -1;
        int gmtModifiedItemIndex = -1;
        boolean needIsDeleted = false;
        XmlElement gmtCreatedEle = null;
        XmlElement creatorEle = null;
        for (Element e : elements) {
            if (e instanceof XmlElement) {
                setItem = (XmlElement) e;
                for (int i = 0; i < setItem.getElements().size(); i++) {
                    XmlElement xmlElement = (XmlElement) setItem.getElements().get(i);
                    for (Attribute att : xmlElement.getAttributes()) {
                        if (att.getValue().equals("modifier != null")) {
                            modifierItemIndex = i;
                            break;
                        }
                        if (att.getValue().equals("gmtModified != null")) {
                            gmtModifiedItemIndex = i;
                            break;
                        }
                        if (att.getValue().equals("isDeleted != null")) {
                            needIsDeleted = true;
                            break;
                        }
                        if (att.getValue().equals("gmtCreated != null")) {
                            gmtCreatedEle = xmlElement;
                            break;
                        }
                        if (att.getValue().equals("creator != null")) {
                            creatorEle = xmlElement;
                            break;
                        }
                    }
                }
            }
        }

        if (setItem != null) {
            if (modifierItemIndex != -1) {
                addModifierXmlElement(setItem, modifierItemIndex);
            }
            if (gmtModifiedItemIndex != -1) {
                addGmtModifiedXmlElement(setItem, gmtModifiedItemIndex);
            }
            if (gmtCreatedEle != null) {
                setItem.getElements().remove(gmtCreatedEle);
            }
            if (creatorEle != null) {
                setItem.getElements().remove(creatorEle);
            }
        }
        if (needIsDeleted) {
            TextElement text = new TextElement(SQL_MAP_COMMON_SUFFIX);
            element.addElement(text);
        }
        return super.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 为Mapper类添加注释（model和mapper通用）
     *
     * @param interfaze
     * @param introspectedTable
     * @param isExtFile
     */
    private void addMapperClassComment(Interface interfaze, IntrospectedTable introspectedTable, boolean isExtFile){
        StringBuilder sb = new StringBuilder();
        interfaze.addJavaDocLine("/**");
        interfaze.addJavaDocLine(" * ");
        // 1.获取表名并添加到JavaDoc中
        if(!isExtFile){
            sb.append(" * ").append(introspectedTable.getFullyQualifiedTable()).append(" 表对应的Mapper类");
        }else{
            String className = interfaze.getType().getShortName();
            sb.append(" * ").append(className.substring(0, className.indexOf("Ext"))).append("的扩展 mapper 接口");
        }
        interfaze.addJavaDocLine(sb.toString());
        interfaze.addJavaDocLine(" * ");
        // 2.添加作者和日期信息到JavaDoc中
        String author = properties.getProperty(PropertyRegistry.AUTHOR);
        if (author == null) {
            author = "$author";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        interfaze.addJavaDocLine(" * @author \t" + author);
        interfaze.addJavaDocLine(" * @date \t" + sdf.format(new Date()));
        interfaze.addJavaDocLine(" */");
    }

    /**
     * 更新xml文档的namespace
     *
     * @param introspectedTable
     * @param parentElement
     */
    private void updateDocumentNameSpace(IntrospectedTable introspectedTable, XmlElement parentElement) {
        Attribute namespaceAttribute = null;
        for (Attribute attribute : parentElement.getAttributes()) {
            if (attribute.getName().equals("namespace")) {
                namespaceAttribute = attribute;
            }
        }
        parentElement.getAttributes().remove(namespaceAttribute);
        parentElement.getAttributes().add(new Attribute("namespace",
                introspectedTable.getMyBatis3JavaMapperType() + JAVA_FILE_SUFFIX));
    }

    /**
     * 若修改时，修改时间为空，则默认为当前时间
     *
     * @param xmlElement
     * @param gmtModifiedItemIndex
     */
    private void addGmtModifiedXmlElement(XmlElement xmlElement, int gmtModifiedItemIndex) {
        XmlElement defaultGmtModified = new XmlElement("if");
        defaultGmtModified.addAttribute(new Attribute("test", "gmtModified == null"));
        defaultGmtModified.addElement(new TextElement("gmt_modified = now(),"));
        xmlElement.getElements().add(gmtModifiedItemIndex + 1, defaultGmtModified);
    }

    /**
     * 若修改时，修改人默认为空，则默认为System
     *
     * @param xmlElement
     * @param modifierItemIndex
     */
    private void addModifierXmlElement(XmlElement xmlElement, int modifierItemIndex) {
        XmlElement defaultModifier = new XmlElement("if");
        defaultModifier.addAttribute(new Attribute("test", "modifier == null"));
        defaultModifier.addElement(new TextElement("modifier = 'System',"));
        xmlElement.getElements().add(modifierItemIndex + 1, defaultModifier);
    }

    /**
     * 是否存在Ext文件
     *
     * @param targetProject
     * @param targetPackage
     * @param fileName
     * @return
     */
    private boolean isExistExtFile(String targetProject, String targetPackage, String fileName) {
        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                return true;
            }
        }

        File testFile = new File(directory, fileName);
        if (testFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}
