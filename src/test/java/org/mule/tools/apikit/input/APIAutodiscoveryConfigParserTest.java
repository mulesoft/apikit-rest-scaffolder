package org.mule.tools.apikit.input;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.apikit.TestUtils;
import org.mule.tools.apikit.input.parsers.APIAutodiscoveryConfigParser;
import org.mule.tools.apikit.model.APIAutodiscoveryConfig;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class APIAutodiscoveryConfigParserTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() throws JDOMException, IOException {
        Document documentFromStream = TestUtils.getDocumentFromStream(TestUtils.getResourceAsStream("api-autodiscovery-parser/file-with-autodiscovery.xml"));
        List<APIAutodiscoveryConfig> apiAutodiscoveryConfigs = new APIAutodiscoveryConfigParser().parse(documentFromStream);
        APIAutodiscoveryConfig apiAutodiscoveryConfig = apiAutodiscoveryConfigs.stream().findFirst().get();
        assertEquals(apiAutodiscoveryConfig.getApiId(), "123");
        assertEquals(apiAutodiscoveryConfig.getFlowRef(), "new-api-main");
        assertEquals(apiAutodiscoveryConfig.getIgnoreBasePath(), Boolean.TRUE);
    }

    @Test(expected = RuntimeException.class)
    public void testFailure() throws JDOMException, IOException, RuntimeException {
        Document documentFromStream = TestUtils.getDocumentFromStream(TestUtils.getResourceAsStream("api-autodiscovery-parser/file-without-autodiscovery.xml"));
        try {
            new APIAutodiscoveryConfigParser().parse(documentFromStream);
        } catch (RuntimeException ex) {
            assertEquals(ex.getMessage(), "apiId is a mandatory field");
            throw ex;
        }
    }
}
