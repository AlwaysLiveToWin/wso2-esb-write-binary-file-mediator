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

package fi.mystes.synapse.mediator.factory;

import fi.mystes.synapse.mediator.WriteBinaryFileMediator;
import fi.mystes.synapse.mediator.WriteBinaryFileMediatorConfigConstants;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.synapse.SynapseException;
import org.junit.Test;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by esa on 10.2.2015.
 */
public class WriteBinaryFileMediatorFactoryTest {
    private static final String DEFAULT_NS_1_PREFIX = "ns1";
    private static final String DEFAULT_NS_1 = "http://ns1.mystes.fi";
    private static final String DEFAULT_NS_2_PREFIX = "ns2";
    private static final String DEFAULT_NS_2 = "http://ns2.mystes.fi";
    private static final String DEFAULT_BINARY_ELEMENT_XPATH = "//blob";
    private static final String DEFAULT_TARGET_DIRECTORY = "/tmp";
    private static final String DEFAULT_TARGET_FILE_NAME = "temp.tmp";


    @Test(expected = SynapseException.class)
    public void mediatorCreationFailsWhenBinaryElementXPathElementMissing() {
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test(expected = SynapseException.class)
    public void mediatorCreationFailsWhenTargetDirectoryElementMissing() {
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test(expected = SynapseException.class)
    public void mediatorCreationFailsWhenTargetFileNameElementMissing() {
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test
    public void configuresBinaryElementXPathFromValueAttribute() {
        OMElement mediatorDefinition = mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems().build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Binary element XPath not configured properly", DEFAULT_BINARY_ELEMENT_XPATH, mediator.getBinaryElementXPath().toString());
    }

    @Test(expected = SynapseException.class)
    public void refusesToInitMediatorWithInvalidBinaryElementXPath() {
        String binaryElementXPath = "**";
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(binaryElementXPath).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test
    public void addsNamespacesFromBinaryElementXPathDefinitionToMediatorConfiguration() {
        String binaryElementXPath = "//ns1:root/ns2:blob";
        Map<String, String> namespaceDefinitions = defaultNamespaceDefinitionsMap();
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(binaryElementXPath, namespaceDefinitions).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertDefaultNamespaceDefinitionsExistInXpath(mediator.getBinaryElementXPath());
    }

    @Test
    public void configuresTargetDirectoryFromValueAttribute() {
        OMElement mediatorDefinition = mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems().build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Target directory not configured properly", DEFAULT_TARGET_DIRECTORY, mediator.getTargetDirectory());
    }

    @Test
    public void configuresTargetDirectoryFromExpressionAttribute() {
        String targetDirectoryExpression = "$ctx:targetDirectory";
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(targetDirectoryExpression, ValueType.EXPRESSION).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Target directory not configured properly", targetDirectoryExpression, mediator.getTargetDirectoryExpression().toString());
    }

    @Test(expected = SynapseException.class)
    public void refusesToInitMediatorWithInvalidTargetDirectoryExpressionXPath() {
        String targetDirectoryExpression = "**";
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(targetDirectoryExpression, ValueType.EXPRESSION).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test
    public void addsNamespacesFromTargetDirectoryElementToTargetDirectoryExpression() {
        String targetDirectoryExpression = "$ctx:targetDirectory";
        Map<String, String> namespaceDefinitions = defaultNamespaceDefinitionsMap();
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(targetDirectoryExpression, ValueType.EXPRESSION, namespaceDefinitions).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertDefaultNamespaceDefinitionsExistInXpath(mediator.getTargetDirectoryExpression());
    }

    @Test
    public void configuresTargetFileNameFromValueAttribute() {
        OMElement mediatorDefinition = mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems().build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Target file name not configured properly", DEFAULT_TARGET_FILE_NAME, mediator.getTargetFileName());
    }

    @Test
    public void configuresTargetFileNameFromExpressionAttribute() {
        String targetFileNameExpression = "//ns1:targetFileName";
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(targetFileNameExpression, ValueType.EXPRESSION).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Target file name not configured properly", targetFileNameExpression, mediator.getTargetFileNameExpression().toString());
    }

    @Test(expected = SynapseException.class)
    public void refusesToInitMediatorWithInvalidTargetFileNameExpressionXPath() {
        String targetFileNameExpression = "**";
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(targetFileNameExpression, ValueType.EXPRESSION).build();
        instantiateMediatorUsingFactory(mediatorDefinition);
    }

    @Test
    public void addsNamespacesFromTargetFileNameElementToTargetFileNameExpression() {
        String targetFileNameExpression = "//ns1:targetFileName";
        Map<String, String> namespaceDefinitions = defaultNamespaceDefinitionsMap();
        OMElement mediatorDefinition = mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(targetFileNameExpression, ValueType.EXPRESSION, namespaceDefinitions).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertDefaultNamespaceDefinitionsExistInXpath(mediator.getTargetFileNameExpression());
    }

    @Test
    public void configuresForceUniqueFileNameFromValueAttribute() {
        String forceUniqueFileName = Boolean.TRUE.toString();
        OMElement mediatorDefinition = mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems().withForceUniqueFileName(forceUniqueFileName).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Force unique file name not configured properly", forceUniqueFileName, mediator.getForceUniqueFileName());
    }

    @Test
    public void configuresAllowOverwriteFromValueAttribute() {
        String allowOverwrite = Boolean.FALSE.toString();
        OMElement mediatorDefinition = mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems().withAllowOverwrite(allowOverwrite).build();
        WriteBinaryFileMediator mediator = instantiateMediatorUsingFactory(mediatorDefinition);
        assertEquals("Allow overwrite not configured properly", allowOverwrite, mediator.getAllowOverWrite());
    }

    private MediatorDefinitionBuilder mediatorDefinitionBuilderWithDefaultsForMandatoryConfigurationItems() {
        return mediatorDefinitionBuilder().withBinaryElementXPath(DEFAULT_BINARY_ELEMENT_XPATH).withTargetDirectory(DEFAULT_TARGET_DIRECTORY, ValueType.VALUE).withTargetFileName(DEFAULT_TARGET_FILE_NAME, ValueType.VALUE);
    }

    private void assertDefaultNamespaceDefinitionsExistInXpath(AXIOMXPath xpath) {
        Map namespaceDefinitions = xpath.getNamespaces();
        assertEquals("Namespace definition missing from XPath " + xpath, DEFAULT_NS_1, namespaceDefinitions.get(DEFAULT_NS_1_PREFIX));
        assertEquals("Namespace definition missing from XPath " + xpath, DEFAULT_NS_1, namespaceDefinitions.get(DEFAULT_NS_1_PREFIX));
    }

    private Map<String, String> defaultNamespaceDefinitionsMap() {
        Map<String, String> namespaceDefinitions = new HashMap<String, String>();
        namespaceDefinitions.put(DEFAULT_NS_1_PREFIX, DEFAULT_NS_1);
        namespaceDefinitions.put(DEFAULT_NS_2_PREFIX, DEFAULT_NS_2);
        return namespaceDefinitions;
    }

    private WriteBinaryFileMediator instantiateMediatorUsingFactory(OMElement mediatorDefinition) {
        WriteBinaryFileMediatorFactory factory = new WriteBinaryFileMediatorFactory();
        WriteBinaryFileMediator mediator = (WriteBinaryFileMediator) factory.createMediator(mediatorDefinition, null);

        return mediator;
    }

    private MediatorDefinitionBuilder mediatorDefinitionBuilder() {
        return new MediatorDefinitionBuilderImpl();
    }

    private enum ValueType {VALUE, EXPRESSION}

    private static interface MediatorDefinitionBuilder {
        MediatorDefinitionBuilder withBinaryElementXPath(String xpath);

        MediatorDefinitionBuilder withBinaryElementXPath(String xpath, Map<String, String> namespaceDefinitions);

        MediatorDefinitionBuilder withTargetDirectory(String targetDirectory, ValueType valueType);

        MediatorDefinitionBuilder withTargetDirectory(String targetDirectory, ValueType valueType, Map<String, String> namespaceDefinitions);

        MediatorDefinitionBuilder withTargetFileName(String targetFileName, ValueType valueType);

        MediatorDefinitionBuilder withTargetFileName(String targetFileName, ValueType valueType, Map<String, String> namespaceDefinitions);

        MediatorDefinitionBuilder withForceUniqueFileName(String value);

        MediatorDefinitionBuilder withAllowOverwrite(String value);

        OMElement build();
    }

    private static final class MediatorDefinitionBuilderImpl implements MediatorDefinitionBuilder {
        private ValueHolder binaryElementXPath;
        private ValueHolder targetDirectory;
        private ValueHolder targetFileName;
        private ValueHolder forceUniqueFileName;
        private ValueHolder allowOverwrite;
        private Map<String, String> binaryElementXPathNsDefs;
        private Map<String, String> targetDirectoryExpressionNsDefs;
        private Map<String, String> targetFileNameExpressionNsDefs;

        @Override
        public MediatorDefinitionBuilder withBinaryElementXPath(String xpath) {
            this.binaryElementXPath = new ValueHolder(xpath, ValueType.VALUE);
            return this;
        }

        @Override
        public MediatorDefinitionBuilder withBinaryElementXPath(String xpath, Map<String, String> namespaceDefinitions) {
            this.binaryElementXPathNsDefs = new HashMap<String, String>(namespaceDefinitions);
            return this.withBinaryElementXPath(xpath);
        }

        @Override
        public MediatorDefinitionBuilder withTargetDirectory(String targetDirectory, ValueType valueType) {
            this.targetDirectory = new ValueHolder(targetDirectory, valueType);
            return this;
        }

        @Override
        public MediatorDefinitionBuilder withTargetDirectory(String targetDirectory, ValueType valueType, Map<String, String> namespaceDefinitions) {
            this.targetDirectoryExpressionNsDefs = new HashMap<String, String>(namespaceDefinitions);
            return this.withTargetDirectory(targetDirectory, valueType);
        }

        @Override
        public MediatorDefinitionBuilder withTargetFileName(String targetFileName, ValueType valueType) {
            this.targetFileName = new ValueHolder(targetFileName, valueType);
            return this;
        }

        @Override
        public MediatorDefinitionBuilder withTargetFileName(String targetFileName, ValueType valueType, Map<String, String> namespaceDefinitions) {
            this.targetFileNameExpressionNsDefs = new HashMap<String, String>(namespaceDefinitions);
            return this.withTargetFileName(targetFileName, valueType);
        }

        @Override
        public MediatorDefinitionBuilder withForceUniqueFileName(String value) {
            forceUniqueFileName = new ValueHolder(value, ValueType.VALUE);
            return this;
        }

        @Override
        public MediatorDefinitionBuilder withAllowOverwrite(String value) {
            this.allowOverwrite = new ValueHolder(value, ValueType.VALUE);
            return this;
        }

        @Override
        public OMElement build() {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement rootElement = factory.createOMElement(WriteBinaryFileMediatorConfigConstants.ROOT_TAG);
            addChildElementIfValueSpecified(factory, rootElement, WriteBinaryFileMediatorConfigConstants.BINARY_ELEMENT_XPATH_TAG, binaryElementXPath, binaryElementXPathNsDefs);
            addChildElementIfValueSpecified(factory, rootElement, WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG, targetDirectory, targetDirectoryExpressionNsDefs);
            addChildElementIfValueSpecified(factory, rootElement, WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG, targetFileName, targetFileNameExpressionNsDefs);
            addChildElementIfValueSpecified(factory, rootElement, WriteBinaryFileMediatorConfigConstants.FORCE_UNIQUE_FILE_NAME_TAG, forceUniqueFileName, null);
            addChildElementIfValueSpecified(factory, rootElement, WriteBinaryFileMediatorConfigConstants.ALLOW_OVERWRITE_TAG, allowOverwrite, null);
            return rootElement;
        }

        private void addChildElementIfValueSpecified(OMFactory factory, OMElement parent, QName childElementQName, ValueHolder valueHolder, Map<String, String> namespaceDefinitions) {
            if (valueHolder != null) {
                OMElement child = factory.createOMElement(childElementQName);
                child.addAttribute(valueHolder.getValueType().toString().toLowerCase(), valueHolder.getValue(), null);
                if (namespaceDefinitions != null) {
                    for (String prefix : namespaceDefinitions.keySet()) {
                        child.declareNamespace(namespaceDefinitions.get(prefix), prefix);
                    }
                }
                parent.addChild(child);
            }
        }
    }

    private static final class ValueHolder {
        private final String value;
        private final ValueType valueType;

        private ValueHolder(String value, ValueType valueType) {
            if (valueType == null) {
                throw new NullPointerException("valueType cannot be null");
            }
            this.value = value;
            this.valueType = valueType;
        }

        public String getValue() {
            return value;
        }

        public ValueType getValueType() {
            return valueType;
        }
    }
}
