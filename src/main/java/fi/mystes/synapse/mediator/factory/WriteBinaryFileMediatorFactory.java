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
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.xml.namespace.QName;
import java.util.Properties;

/**
 * Created by esa on 10.2.2015.
 */
public class WriteBinaryFileMediatorFactory extends AbstractMediatorFactory {
    @Override
    protected Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        WriteBinaryFileMediator mediator = new WriteBinaryFileMediator();
        processAuditStatus(mediator, omElement);

        configureBinaryElementXPath(omElement, mediator);
        configureTargetDirectory(omElement, mediator);
        configureTargetFileName(omElement, mediator);
        configureForceUniqueFileName(omElement, mediator);
        configureAllowOverwrite(omElement, mediator);

        return mediator;
    }

    @Override
    public QName getTagQName() {
        return WriteBinaryFileMediatorConfigConstants.ROOT_TAG;
    }

    private void configureTargetDirectory(OMElement omElement, WriteBinaryFileMediator mediator) {
        OMElement targetDirectoryElement = omElement.getFirstChildWithName(WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG);
        if (targetDirectoryElement == null) {
            handleException(errorMessageForMissingMandatoryConfigurationElement(WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG));
        }
        String valueAttribute = targetDirectoryElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE);
        if (valueAttribute != null) {
            mediator.setTargetDirectory(valueAttribute);
        } else {
            String expressionAttribute = targetDirectoryElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_EXPRESSION);
            try {
                SynapseXPath targetDirectoryExpression = new SynapseXPath(expressionAttribute);
                targetDirectoryExpression.addNamespaces(targetDirectoryElement);
                mediator.setTargetDirectoryExpression(targetDirectoryExpression);
            } catch (JaxenException e) {
                handleException("Invalid target directory XPath in mediator configuration", e);
            }
        }
    }

    private void configureTargetFileName(OMElement omElement, WriteBinaryFileMediator mediator) {
        OMElement targetFileNameElement = omElement.getFirstChildWithName(WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG);
        if (targetFileNameElement == null) {
            handleException(errorMessageForMissingMandatoryConfigurationElement(WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG));
        }
        String valueAttribute = targetFileNameElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE);
        if (valueAttribute != null) {
            mediator.setTargetFileName(valueAttribute);
        } else {
            String expressionAttribute = targetFileNameElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_EXPRESSION);
            try {
                SynapseXPath targetFileNameExpression = new SynapseXPath(expressionAttribute);
                targetFileNameExpression.addNamespaces(targetFileNameElement);
                mediator.setTargetFileNameExpression(targetFileNameExpression);
            } catch (JaxenException e) {
                handleException("Invalid target file name XPath in mediator configuration", e);
            }
        }
    }

    private void configureBinaryElementXPath(OMElement omElement, WriteBinaryFileMediator mediator) {
        OMElement binaryElementXPathElement = omElement.getFirstChildWithName(WriteBinaryFileMediatorConfigConstants.BINARY_ELEMENT_XPATH_TAG);
        if (binaryElementXPathElement == null) {
            handleException(errorMessageForMissingMandatoryConfigurationElement(WriteBinaryFileMediatorConfigConstants.BINARY_ELEMENT_XPATH_TAG));
        }
        String valueAttribute = binaryElementXPathElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE);
        if (valueAttribute != null) {
            try {
                SynapseXPath xpath = new SynapseXPath(valueAttribute);
                xpath.addNamespaces(binaryElementXPathElement);
                mediator.setBinaryElementXPath(xpath);
            } catch (JaxenException e) {
                handleException("Invalid binary element XPath in mediator configuration", e);
            }
        }
    }

    private void configureForceUniqueFileName(OMElement omElement, WriteBinaryFileMediator mediator) {
        OMElement forceUniqueFileNameElement = omElement.getFirstChildWithName(WriteBinaryFileMediatorConfigConstants.FORCE_UNIQUE_FILE_NAME_TAG);

        if (forceUniqueFileNameElement != null) {
            String valueAttribute = forceUniqueFileNameElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE);
            mediator.setForceUniqueFileName(valueAttribute);
        }
    }

    private void configureAllowOverwrite(OMElement omElement, WriteBinaryFileMediator mediator) {
        OMElement allowOverwriteElement = omElement.getFirstChildWithName(WriteBinaryFileMediatorConfigConstants.ALLOW_OVERWRITE_TAG);

        if (allowOverwriteElement != null) {
            String valueAttribute = allowOverwriteElement.getAttributeValue(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE);
            mediator.setAllowOverWrite(valueAttribute);
        }
    }

    private String errorMessageForMissingMandatoryConfigurationElement(QName elementQName) {
        return String.format("Missing mandatory configuration element %1$s in %2$s mediator configuration",
                elementQName.getLocalPart(), WriteBinaryFileMediatorConfigConstants.ROOT_TAG_NAME);
    }


}
