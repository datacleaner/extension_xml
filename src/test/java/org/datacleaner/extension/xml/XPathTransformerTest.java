package org.datacleaner.extension.xml;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import org.datacleaner.api.ComponentContext;
import org.datacleaner.api.OutputColumns;
import org.datacleaner.data.MockInputColumn;
import org.datacleaner.data.MockInputRow;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class XPathTransformerTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    
    private static final String EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<books>"
                + "<book>Robinson Crusoe</book>"
                + "<book>Gulliver's Travels</book>"
            + "</books>";
    
    private static final String INPUT_COLUMN_NAME = "xml value";

    private static final String XPATH_QUERY_FIRST_BOOK = "/books/book[1]/text()";
    
    private static final String XPATH_QUERY_SECOND_BOOK = "/books/book[2]/text()";

    private XPathTransformer xPathTransformer;

    @Before
    public void setUp() {
        xPathTransformer = new XPathTransformer();

        MockInputColumn<String> xmlInputColumn = new MockInputColumn<String>(INPUT_COLUMN_NAME);

        xPathTransformer.column = xmlInputColumn;
        
        xPathTransformer.componentContext = EasyMock.createMock(ComponentContext.class);
    }

    @Test
    public void testSingleXPathQuery() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        xPathTransformer.xPathExpressions = new String[] { XPATH_QUERY_FIRST_BOOK };
        xPathTransformer.init();

        assertThat(xPathTransformer.transform(inputRow)[0], is(Arrays.asList(new String[] {"Robinson Crusoe"})));
        OutputColumns outputColumns = xPathTransformer.getOutputColumns();

        assertEquals(1, outputColumns.getColumnCount());
        assertEquals(INPUT_COLUMN_NAME + " (" + XPATH_QUERY_FIRST_BOOK + ")", outputColumns.getColumnName(0));
        assertEquals(String.class, outputColumns.getColumnType(0));
    }

    @Test
    public void testMultipleXPathQueries() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        xPathTransformer.xPathExpressions = new String[] { XPATH_QUERY_FIRST_BOOK, XPATH_QUERY_SECOND_BOOK };
        xPathTransformer.init();

        assertThat(xPathTransformer.transform(inputRow)[0], is(Arrays.asList(new String[] {"Robinson Crusoe"})));
        assertThat(xPathTransformer.transform(inputRow)[1], is(Arrays.asList(new String[] {"Gulliver's Travels"})));

        OutputColumns outputColumns = xPathTransformer.getOutputColumns();

        assertEquals(2, outputColumns.getColumnCount());
        assertEquals(INPUT_COLUMN_NAME
                + " (" + XPATH_QUERY_FIRST_BOOK + ")", outputColumns.getColumnName(0));
        assertEquals(INPUT_COLUMN_NAME
                + " (" + XPATH_QUERY_SECOND_BOOK + ")", outputColumns.getColumnName(1));
    }

    @Test
    public void testIllegalXPathQuery() {
        xPathTransformer.xPathExpressions = new String[] { "<abracadabra>" };
        
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Error occurred compiling XPath expression: \"<abracadabra>\"");
        xPathTransformer.init();
    }

    @Test
    public void testUnparsableXMLSource() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { "" });

        xPathTransformer.xPathExpressions = new String[] { XPATH_QUERY_FIRST_BOOK };
        xPathTransformer.init();

        assertThat(xPathTransformer.transform(inputRow)[0], is(Arrays.asList(new String[] {})));
    }

    @Test
    public void testNullXMLSource() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { null });

        xPathTransformer.xPathExpressions = new String[] { XPATH_QUERY_FIRST_BOOK };
        xPathTransformer.init();

        assertThat(xPathTransformer.transform(inputRow)[0], is(Arrays.asList(new String[] {})));
    }

    @Test
    public void testSingleXPathQueryWithMultipleResults() {
        MockInputRow inputRow = new MockInputRow(new MockInputColumn<?>[] {
                (MockInputColumn<?>) xPathTransformer.column }, new String[] { EXAMPLE_XML });

        final String xPathQueryAllBooks = "/books/book/text()";
        
        xPathTransformer.xPathExpressions = new String[] { xPathQueryAllBooks };
        xPathTransformer.init();

        assertThat(xPathTransformer.transform(inputRow)[0], is(Arrays.asList(new String[] { "Robinson Crusoe",
                "Gulliver's Travels" })));

        OutputColumns outputColumns = xPathTransformer.getOutputColumns();

        assertEquals(1, outputColumns.getColumnCount());
        assertEquals(INPUT_COLUMN_NAME + " (" + xPathQueryAllBooks + ")", outputColumns.getColumnName(0));
        assertEquals(String.class, outputColumns.getColumnType(0));
    }
}
