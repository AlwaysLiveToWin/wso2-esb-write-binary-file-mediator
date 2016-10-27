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

package fi.mystes.synapse.mediator.serializer;

import fi.mystes.synapse.mediator.WriteBinaryFileMediator;
import fi.mystes.synapse.mediator.WriteBinaryFileMediatorConfigConstants;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.junit.Test;

import javax.xml.namespace.QName;

import static org.junit.Assert.*;

/**
 * Created by esa on 25.2.2015.
 */
public class WriteBinaryFileMediatorSerializerTest {
    private static final String DEFAULT_NS_1_PREFIX = "m1";
    private static final String DEFAULT_NS_1 = "http://ns1.synapse.mediator.mystes.fi";
    private static final String DEFAULT_NS_2_PREFIX = "m2";
    private static final String DEFAULT_NS_2 = "http://ns2.synapse.mediator.mystes.fi";

    @Test
    public void serializesRootElementWithProperLocalNameAndNamespace() {
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();

        OMElement mediatorDefinition = doSerialize(mediator);

        assertEquals("Invalid mediator root element", WriteBinaryFileMediatorConfigConstants.ROOT_TAG, mediatorDefinition.getQName());
    }

    @Test
    public void serializesBinaryElementXPathFromValueAttribute() throws JaxenException {
        String binaryElementXPath = "//ns1:root/ns2:image";
        SynapseXPath xPath = new SynapseXPath(binaryElementXPath);
        addDefaultNamespaceDefinitions(xPath);

        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setBinaryElementXPath(xPath);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.BINARY_ELEMENT_XPATH_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE, binaryElementXPath);
        assertDefaultNamespaceDefinitionsExistInElement(configElement);
    }

    @Test
    public void serializesTargetDirectoryFromValueAttribute() {
        String targetDirectory = "/tmp/images";
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setTargetDirectory(targetDirectory);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE, targetDirectory);
    }

    @Test
    public void serializesTargetDirectoryFromExpressionAttribute() throws JaxenException {
        SynapseXPath targetDirectoryExpression = new SynapseXPath("$ctx:targetDirectory");
        addDefaultNamespaceDefinitions(targetDirectoryExpression);
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setTargetDirectoryExpression(targetDirectoryExpression);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_EXPRESSION, targetDirectoryExpression.toString());
        assertDefaultNamespaceDefinitionsExistInElement(configElement);
    }

    @Test
    public void serializesTargetFileNameFromValueAttribute() {
        String targetFileName = "image.png";
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setTargetFileName(targetFileName);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE, targetFileName);
    }

    @Test
    public void serializesTargetFileNameFromExpressionAttribute() throws JaxenException {
        SynapseXPath targetFileNameExpression = new SynapseXPath("$ctx:targetFileName");
        addDefaultNamespaceDefinitions(targetFileNameExpression);
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setTargetFileNameExpression(targetFileNameExpression);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_EXPRESSION, targetFileNameExpression.toString());
        assertDefaultNamespaceDefinitionsExistInElement(configElement);
    }

    @Test
    public void serializesForceUniqueFileNameFromValueAttribute() {
        String forceUniqueFileName = "true";
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setForceUniqueFileName(forceUniqueFileName);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.FORCE_UNIQUE_FILE_NAME_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE, forceUniqueFileName);
    }

    @Test
    public void serializesAllowOverwriteFromValueAttribute() {
        String allowOverwrite = "false";
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        mediator.setAllowOverWrite(allowOverwrite);

        OMElement mediatorDefinition = doSerialize(mediator);

        OMElement configElement = assertChildElementExists(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.ALLOW_OVERWRITE_TAG);
        assertConfigurationAttributeValue(configElement, WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE, allowOverwrite);
    }

    @Test
    public void doesNotSerializeNonMandatoryConfigurationElementsWhenValuesNotSpecified() {
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        OMElement mediatorDefinition = doSerialize(mediator);
        assertChildElementDoesNotExist(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.FORCE_UNIQUE_FILE_NAME_TAG);
        assertChildElementDoesNotExist(mediatorDefinition, WriteBinaryFileMediatorConfigConstants.ALLOW_OVERWRITE_TAG);
    }

    private void assertChildElementDoesNotExist(OMElement mediatorDefinition, QName elementQName) {
        assertNull(elementQName.getLocalPart() + " child element should not exist in mediator definition", mediatorDefinition.getFirstChildWithName(elementQName));
    }

    private void assertDefaultNamespaceDefinitionsExistInElement(OMElement element) {
        assertNotNull("Missing default namespace definition " + DEFAULT_NS_1_PREFIX, element.findNamespace(DEFAULT_NS_1, DEFAULT_NS_1_PREFIX));
        assertNotNull("Missing default namespace definition " + DEFAULT_NS_2_PREFIX, element.findNamespace(DEFAULT_NS_2, DEFAULT_NS_2_PREFIX));
    }

    private void addDefaultNamespaceDefinitions(SynapseXPath xPath) throws JaxenException {
        xPath.addNamespace(DEFAULT_NS_1_PREFIX, DEFAULT_NS_1);
        xPath.addNamespace(DEFAULT_NS_2_PREFIX, DEFAULT_NS_2);
    }

    private void assertConfigurationAttributeValue(OMElement element, QName attributeQName, String expectedAttributeValue) {
        OMAttribute attribute = element.getAttribute(attributeQName);
        assertNotNull("Missing attribute '" + attributeQName.getLocalPart() + "' for element '" + element.getLocalName() + "'", attribute);
        assertEquals("Value for '" + attribute.getLocalName() + "' attribute not serialized correctly", expectedAttributeValue, attribute.getAttributeValue());
    }

    private OMElement assertChildElementExists(OMElement mediatorDefinition, QName expectedChildElementQName) {
        OMElement binaryElementXPathElement = mediatorDefinition.getFirstChildWithName(expectedChildElementQName);
        assertNotNull("Missing child element '" + expectedChildElementQName.getLocalPart() + "' for mediator definition", binaryElementXPathElement);
        return binaryElementXPathElement;
    }

    private OMElement doSerialize(WriteBinaryFileMediator mediator) {
        return new WriteBinaryFileMediatorSerializer().serializeMediator(null, mediator);
    }
}
