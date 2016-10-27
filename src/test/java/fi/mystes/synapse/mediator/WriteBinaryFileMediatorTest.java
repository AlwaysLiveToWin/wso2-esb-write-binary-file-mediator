/**
 * Copyright 2016 Mystes Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fi.mystes.synapse.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.io.FileUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by esa on 4.2.2015.
 */
public class WriteBinaryFileMediatorTest {

    private static final String DEFAULT_FILE_NAME = "temp.png";
    private static final String EXPECTED_OUTPUT_FILE = "expected-output.png";

    // base64 encoded single black pixel PNG
    private static final String BINARY_DATA = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAMSURBVBhXY2BgYAAAAAQAAVzN/2kAAAAASUVORK5CYII=";

    @Mock
    private MessageContext messageContext;

    @Mock
    private SOAPEnvelope envelope;

    @Mock
    private SOAPBody body;

    private File outputDir;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(messageContext.getEnvelope()).thenReturn(envelope);
        when(envelope.getBody()).thenReturn(body);
    }

    @Before
    public void initOutputDir() {
        String tmpDirPath = System.getProperty("java.io.tmpdir") + File.separator + "mediator";
        outputDir = new File(tmpDirPath);
        assert outputDir.mkdir();
    }

    @After
    public void deleteOutputDir() throws IOException {
        FileUtils.deleteDirectory(outputDir);
    }

    @Test(expected = SynapseException.class)
    public void mediationFailsWhenBinaryElementXpathNotSpecified() {
        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        configureMediatorWithDefaultFilePaths(mediator);
        mediator.mediate(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void mediationFailsWhenTargetDirectoryNotSpecified() throws JaxenException {
        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        mediator.setTargetFileName(DEFAULT_FILE_NAME);
        mediator.setBinaryElementXPath(new SynapseXPath("//binaryContent"));
        mediator.mediate(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void mediationFailsWhenTargetFileNameNotSpecified() throws JaxenException {
        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        mediator.setTargetDirectory(outputDir.getAbsolutePath());
        mediator.setBinaryElementXPath(new SynapseXPath("//binaryContent"));
        mediator.mediate(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void mediationFailsWhenBinaryElementNotFoundInPayload() throws XMLStreamException, JaxenException {
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//notExistingElement");

        mediator.mediate(messageContext);
    }

    @Test
    public void writesBinaryContentToFileSpecifiedByTargetDirectoryAndFileName() throws XMLStreamException, IOException, URISyntaxException, JaxenException {
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertDefaultOutputFileExists();
        assertDefaultOutputFileContent();
    }

    @Test
    public void supportsSpecifyingTargetDirectoryAsXpathExpression() throws XMLStreamException, JaxenException, IOException, URISyntaxException {
        OMElement payload = payloadWithNoNamespaces();
        when(body.getFirstElement()).thenReturn(payload);
        when(messageContext.getProperty("targetDirectory")).thenReturn(outputDir.getAbsolutePath());

        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        mediator.setTargetFileName(DEFAULT_FILE_NAME);
        mediator.setTargetDirectoryExpression(new SynapseXPath("$ctx:targetDirectory"));
        mediator.setBinaryElementXPath(new SynapseXPath("//image"));

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertDefaultOutputFileExists();
        assertDefaultOutputFileContent();
    }

    @Test
    public void supportsSpecifyingTargetFileNameAsXpathExpression() throws XMLStreamException, JaxenException, IOException, URISyntaxException {
        OMElement payload = payloadWithNoNamespaces();
        when(body.getFirstElement()).thenReturn(payload);
        when(messageContext.getProperty("targetFileName")).thenReturn(DEFAULT_FILE_NAME);

        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        mediator.setTargetDirectory(outputDir.getAbsolutePath());
        mediator.setTargetFileNameExpression(new SynapseXPath("$ctx:targetFileName"));
        mediator.setBinaryElementXPath(new SynapseXPath("//image"));

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertDefaultOutputFileExists();
        assertDefaultOutputFileContent();
    }

    @Test
    public void replacesBinaryContentWithOutputFilePathInPayload() throws XMLStreamException, JaxenException {
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        OMElement binaryElement = payload.getFirstChildWithName(new QName(null, "image"));
        assertDefaultFilePathGotWrittenToElement(binaryElement);
    }

    @Test
    public void supportsNamespacesInBinaryElementXPath() throws XMLStreamException, IOException, URISyntaxException, JaxenException {
        OMElement payload = payloadWithNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "/ns1:Entry/ns2:image");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertDefaultOutputFileExists();
        assertDefaultOutputFileContent();
        OMElement binaryElement = payload.getFirstChildWithName(new QName("http://ns2.acme.inc", "image"));
        assertDefaultFilePathGotWrittenToElement(binaryElement);
    }

    @Test
    public void supportsDirectReferenceToTextContentInBinaryElementXPath() throws XMLStreamException, IOException, URISyntaxException, JaxenException {
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image/text()");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        OMElement binaryElement = payload.getFirstChildWithName(new QName(null, "image"));
        assertDefaultOutputFileExists();
        assertDefaultOutputFileContent();
        assertDefaultFilePathGotWrittenToElement(binaryElement);
    }

    @Test
    public void doesNotWriteOutputFileIfBinaryContentElementEmptyInPayload() throws XMLStreamException, JaxenException {
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//empty");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        OMElement emptyElement = payload.getFirstChildWithName(new QName(null, "empty"));
        assertFalse("Output file should not have been written", defaultOutputFile().exists());
        assertEquals("File path should not exist in element", "", emptyElement.getText());
    }

    @Test
    public void generatesUniqueOutputFileNameWhenSpecifiedInConfiguration() throws XMLStreamException, IOException, URISyntaxException, JaxenException {
        String messageId = "urn:uuid:fa5ea72d-1bba-4edf-9799-4131ed17f8e1";
        when(messageContext.getMessageID()).thenReturn(messageId);
        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image");
        mediator.setForceUniqueFileName("true");
        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));

        File expectedOutputFile = new File(outputDir, messageId + "_" + DEFAULT_FILE_NAME);
        assertOutputFileExists(expectedOutputFile);
        assertOutputFileContent(expectedOutputFile);
        OMElement binaryElement = payload.getFirstChildWithName(new QName(null, "image"));
        assertFilePathGotWrittenToElement(binaryElement, expectedOutputFile);
    }

    @Test
    public void overWritesExistingFileByDefault() throws IOException, XMLStreamException, URISyntaxException, JaxenException {
        File outputFile = defaultOutputFile();
        FileUtils.write(outputFile, "someData");

        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image");

        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertDefaultOutputFileContent();
    }

    @Test
    public void doesNotOverwriteExistingFileWhenDeniedInConfiguration() throws XMLStreamException, IOException, URISyntaxException, JaxenException {
        File outputFile = defaultOutputFile();
        FileUtils.write(outputFile, "someData");

        OMElement payload = payloadWithNoNamespaces();
        WriteBinaryFileMediator mediator = initializeMediatorForFileWriteTest(payload, "//image");
        mediator.setAllowOverWrite("false");
        assertTrue("Mediation shouldn't have been terminated", mediator.mediate(messageContext));
        assertEquals("File content shouldn't have been overwritten", "someData", FileUtils.readFileToString(defaultOutputFile()));
        OMElement binaryElement = payload.getFirstChildWithName(new QName(null, "image"));
        assertEquals("Binary data should not have been overwritten in payload", BINARY_DATA, binaryElement.getText());
    }

    private void assertDefaultFilePathGotWrittenToElement(OMElement element) {
        assertFilePathGotWrittenToElement(element, defaultOutputFile());
    }

    private void assertFilePathGotWrittenToElement(OMElement element, File outputFile) {
        assertEquals("File path should have been written as element content", outputFile.getAbsolutePath(), element.getText());
    }

    private void assertDefaultOutputFileContent() throws IOException, URISyntaxException {
        assertOutputFileContent(defaultOutputFile());
    }

    private void assertOutputFileContent(File outputFile) throws IOException, URISyntaxException {
        File expectedFile = expectedOutputFile();

        assertEquals("Unexpected output file content", FileUtils.checksumCRC32(expectedFile), FileUtils.checksumCRC32(outputFile));
    }

    private void assertDefaultOutputFileExists() {
        assertOutputFileExists(defaultOutputFile());
    }

    private void assertOutputFileExists(File outputFile) {
        assertTrue("Mediation output file " + outputFile.getAbsolutePath() + " does not exist", outputFile.exists());
    }

    private File defaultOutputFile() {
        return new File(outputDir, DEFAULT_FILE_NAME);
    }

    private File expectedOutputFile() throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(EXPECTED_OUTPUT_FILE).toURI());
    }

    private WriteBinaryFileMediator initializeMediatorForFileWriteTest(OMElement payloadToUseInTest, String sourceElementXPath) throws JaxenException {
        when(body.getFirstElement()).thenReturn(payloadToUseInTest);
        WriteBinaryFileMediator mediator = createUnConfiguredMediator();
        configureMediatorWithDefaultFilePaths(mediator);
        mediator.setBinaryElementXPath(new SynapseXPath(sourceElementXPath));

        return mediator;
    }

    private OMElement payloadWithNoNamespaces() throws XMLStreamException {
        return AXIOMUtil.stringToOM("<Entry>\n" +
                "<id>8</id>\n" +
                "<image>" + BINARY_DATA + "</image>\n" +
                "<empty/>\n" +
                "</Entry>");
    }

    private OMElement payloadWithNamespaces() throws XMLStreamException {
        return AXIOMUtil.stringToOM("<ns1:Entry xmlns:ns1=\"http://ns1.acme.inc\" xmlns:ns2=\"http://ns2.acme.inc\">\n" +
                "<ns2:id>8</ns2:id>\n" +
                "<ns2:image>" + BINARY_DATA + "</ns2:image>\n" +
                "</ns1:Entry>");
    }

    private WriteBinaryFileMediator createUnConfiguredMediator() {
        return new WriteBinaryFileMediator();
    }

    private void configureMediatorWithDefaultFilePaths(WriteBinaryFileMediator mediator) {
        mediator.setTargetFileName(DEFAULT_FILE_NAME);
        mediator.setTargetDirectory(outputDir.getAbsolutePath());
    }
}
