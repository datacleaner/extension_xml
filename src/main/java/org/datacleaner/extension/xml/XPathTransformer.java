package org.datacleaner.extension.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.ExecutionLogMessage;
import org.datacleaner.api.Initialize;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Provided;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DataStructuresCategory;
import org.datacleaner.components.categories.TransformSuperCategory;
import org.w3c.dom.Document;

@Named("Select values from XML")
@Description("Select values from XML using a number of XPath expressions")
@Categorized(superCategory = TransformSuperCategory.class, value = DataStructuresCategory.class)
public class XPathTransformer implements Transformer {
    @Configured
    InputColumn<String> column;

    @Configured
    @Description("XPath expressions used to retrieve values from inputs.")
    String[] xPathExpressions;

    @Provided
    ComponentContext componentContext;

    private XPathExpression[] compiledExpressions;

    @Initialize
    public void init() {
        final XPath xPath = XPathFactory.newInstance().newXPath();

        compiledExpressions = new XPathExpression[xPathExpressions.length];

        for (int i = 0; i < xPathExpressions.length; i++) {
            try {
                compiledExpressions[i] = xPath.compile(xPathExpressions[i]);
            } catch (XPathExpressionException e) {
                componentContext.publishMessage(new ExecutionLogMessage("Error occurred compiling XPath expression: \""
                        + xPathExpressions[i] + "\"."));
            }
        }
    }

    @Override
    public OutputColumns getOutputColumns() {
        String[] names = new String[xPathExpressions.length];
        for (int i = 0; i < xPathExpressions.length; i++) {
            names[i] = column.getName() + " (" + xPathExpressions[i] + ")";
        }
        return new OutputColumns(String.class, names);
    }

    @Override
    public String[] transform(InputRow inputRow) {
        final String[] result = new String[compiledExpressions.length];

        Document xmlDocument = parseDocument(inputRow.getValue(column));

        for (int i = 0; i < compiledExpressions.length; i++) {
            result[i] = evaluateXPathExpression(compiledExpressions[i], xmlDocument);
        }

        return result;
    }

    private Document parseDocument(String xml) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            try {
                InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());

                return documentBuilder.parse(xmlStream);
            } catch (Exception e) {
                componentContext.publishMessage(new ExecutionLogMessage("Error occurred parsing string as xml document:\n" + xml));
            }
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Failed to create a DocumentBuilder.", e);
        }
    }

    private String evaluateXPathExpression(XPathExpression xPathExpression, Document xmlDocument) {
        if (xPathExpression != null) {
            try {
                return xPathExpression.evaluate(xmlDocument);
            } catch (XPathExpressionException e) {
                componentContext.publishMessage(new ExecutionLogMessage("Error occurred compiling XPath expression: \"" + xPathExpression + "\"."));
            }
        }

        return "";
    }
}
