package org.datacleaner.extension.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.datacleaner.api.Categorized;
import org.datacleaner.api.Configured;
import org.datacleaner.api.Description;
import org.datacleaner.api.InputColumn;
import org.datacleaner.api.InputRow;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.api.Transformer;
import org.datacleaner.components.categories.DataStructuresCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

@Named("Select values from XML")
@Description("Select values from XML using a number of XPath expressions")
@Categorized(DataStructuresCategory.class)
public class XPathTransformer implements Transformer {
    private static final Logger logger = LoggerFactory.getLogger(XPathTransformer.class);

    @Configured
    InputColumn<String> column;

    @Configured
    @Description("XPath expressions used to retrieve values from inputs.")
    String[] xPathExpressions;

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
        final String[] result = new String[xPathExpressions.length];

        Document xmlDocument = parseDocument(inputRow.getValue(column));

        for (int i = 0; i < xPathExpressions.length; i++) {
            result[i] = evaluateXPathExpression(xPathExpressions[i], xmlDocument);
        }

        return result;
    }

    private static Document parseDocument(String xml) {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            try {
                InputStream xmlStream = new ByteArrayInputStream(xml.getBytes());

                return documentBuilder.parse(xmlStream);
            } catch (Exception e) {
                logger.info("Error occured parsing string as xml document: {}", xml);
            }
            return documentBuilder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private static String evaluateXPathExpression(String xPathExpression, Document xmlDocument) {
        final XPath xPath = XPathFactory.newInstance().newXPath();
                
        try {
            return xPath.evaluate(xPathExpression, xmlDocument);
        } catch (XPathExpressionException e) {
            logger.info("Error occured evaluating XPath expression: {}", xPathExpression);
        }
        
        return "";
    }
}
