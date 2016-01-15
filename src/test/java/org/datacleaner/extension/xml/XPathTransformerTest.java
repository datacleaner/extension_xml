package org.datacleaner.extension.xml;

import static org.junit.Assert.assertEquals;

import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.junit.Before;
import org.junit.Test;

public class XPathTransformerTest {
    private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<books>"
                + "<book>Robinson Crusoe</book>"
                + "<book>Gulliver's Travels</book>"
            + "</books>";
    
    private XPathTransformer xPathTransformer;

    @Before
    public void setUp() {
        xPathTransformer = new XPathTransformer();

        MockInputColumn<String> xmlInputColumn = new MockInputColumn<String>("xml value");

        xPathTransformer.column = xmlInputColumn;
    }

    @Test
    public void testSingleXPathQuery() {

        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        xPathTransformer.xPathExpressions = new String[] { "/books/book[1]" };

        assertEquals("Robinson Crusoe", xPathTransformer.transform(inputRow)[0].toString());

        OutputColumns outputColumns = xPathTransformer.getOutputColumns();

        assertEquals(1, outputColumns.getColumnCount());
        assertEquals("xml value (/books/book[1])", outputColumns.getColumnName(0));
        assertEquals(String.class, outputColumns.getColumnType(0));
    }

    @Test
    public void testMultipleXPathQueries() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        xPathTransformer.xPathExpressions = new String[] { "/books/book[1]", "/books/book[2]" };

        assertEquals("Robinson Crusoe", xPathTransformer.transform(inputRow)[0].toString());
        assertEquals("Gulliver's Travels", xPathTransformer.transform(inputRow)[1].toString());

        OutputColumns outputColumns = xPathTransformer.getOutputColumns();

        assertEquals(2, outputColumns.getColumnCount());
        assertEquals("xml value (/books/book[1])", outputColumns.getColumnName(0));
        assertEquals("xml value (/books/book[2])", outputColumns.getColumnName(1));
    }

    @Test
    public void testIllegalXPathQuery() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        xPathTransformer.xPathExpressions = new String[] { "<abracadabra>" };

        assertEquals("", xPathTransformer.transform(inputRow)[0].toString());
    }

    @Test
    public void testUnparsableXMLSource() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { "" });

        xPathTransformer.xPathExpressions = new String[] { "/books/book[1]" };

        assertEquals("", xPathTransformer.transform(inputRow)[0].toString());
    }

    @Test
    public void testNullXMLSource() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { null });

        xPathTransformer.xPathExpressions = new String[] { "/books/book[1]" };

        assertEquals("", xPathTransformer.transform(inputRow)[0].toString());
    }
}
