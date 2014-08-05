/*
 *  Copyright 2009 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import java.sql.Types;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 * @author Jeff Butler
 */
public class ListElementGenerator extends AbstractXmlElementGenerator {

    public ListElementGenerator() {

        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                "list"));
        answer.addAttribute(new Attribute("resultType", super.getFullQualifyDomainObject())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);
        answer.addElement(new TextElement("select " + super.getIncludeAllColumns())); //$NON-NLS-1$ 
        answer.addElement(new TextElement("from " + super.getIncludeTableId())); //$NON-NLS-1$

        XmlElement dynamicElement = new XmlElement("where"); //$NON-NLS-1$
        answer.addElement(dynamicElement);

        String startPrefix = "Start";
        String endPrefix = "End";

        for (IntrospectedColumn introspectedColumn: introspectedTable.getAllColumns()) {

            String javaProperty = introspectedColumn.getJavaProperty();
            String escapedColumnName = MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            String parameterClause = MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);

            XmlElement isNotNullElement = createIsNotNullElement(javaProperty, escapedColumnName, parameterClause, "=");
            dynamicElement.addElement(isNotNullElement);

            int jdbcType = introspectedColumn.getJdbcType();
            if (jdbcType == Types.DATE || jdbcType == Types.TIME || jdbcType == Types.TIMESTAMP) {

                String javaPropertyStart = javaProperty + startPrefix;

                isNotNullElement = createIsNotNullElement(javaPropertyStart, escapedColumnName,
                        parameterClause.replace(javaProperty, javaPropertyStart), ">=");
                dynamicElement.addElement(isNotNullElement);

                String javaPropertyEnd = javaProperty + endPrefix;
                isNotNullElement = createIsNotNullElement(javaPropertyEnd, escapedColumnName,
                        parameterClause.replace(javaProperty, javaPropertyEnd), "<=");
                dynamicElement.addElement(isNotNullElement);

            }
        }
        parentElement.addElement(answer);

    }

    private XmlElement createIsNotNullElement(String javaProperty, String escapeColumnName, String parameterClause,
            String op) {

        StringBuilder sb = new StringBuilder();
        XmlElement isNotNullElement = new XmlElement("if"); //$NON-NLS-1$
        sb.setLength(0);
        sb.append(javaProperty);
        sb.append(" != null"); //$NON-NLS-1$
        isNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$ 

        sb.setLength(0);
        sb.append("and ");

        sb.append(escapeColumnName);
        if (isNeedEscape(op)) {
            sb.append(" <![CDATA[");
        }

        sb.append(" " + op + " "); //$NON-NLS-1$
        if (isNeedEscape(op)) {
            sb.append("]]> ");
        }

        sb.append(parameterClause);

        isNotNullElement.addElement(new TextElement(sb.toString()));

        return isNotNullElement;

    }

    private boolean isNeedEscape(String op) {

        return op.equals("<=") || op.equals(">=");
    }

}
