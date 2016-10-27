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
import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;

import javax.xml.namespace.QName;

/**
 * Created by esa on 25.2.2015.
 */
public class WriteBinaryFileMediatorSerializer extends AbstractMediatorSerializer {

    @Override
    protected OMElement serializeSpecificMediator(Mediator m) {
        WriteBinaryFileMediator mediator = (WriteBinaryFileMediator) m;

        OMElement rootElement = fac.createOMElement(WriteBinaryFileMediatorConfigConstants.ROOT_TAG_NAME, synNS);

        addConfigElement(rootElement, serializeBinaryElementXPath(mediator));
        addConfigElement(rootElement, serializeTargetDirectory(mediator));
        addConfigElement(rootElement, serializeTargetFileName(mediator));
        addConfigElement(rootElement, serializeForceUniqueFileName(mediator));
        addConfigElement(rootElement, serializeAllowOverwrite(mediator));

        saveTracingState(rootElement, mediator);

        return rootElement;
    }

    @Override
    public String getMediatorClassName() {
        return WriteBinaryFileMediator.class.getName();
    }

    private void addConfigElement(OMElement mediatorDefinition, OMElement element) {
        if (element != null) {
            mediatorDefinition.addChild(element);
        }
    }

    private OMElement serializeBinaryElementXPath(WriteBinaryFileMediator mediator) {
        OMElement element = createConfigElement(WriteBinaryFileMediatorConfigConstants.BINARY_ELEMENT_XPATH_TAG);

        if (mediator.getBinaryElementXPath() != null) {
            addValueAttribute(element, mediator.getBinaryElementXPath().toString());
            serializeNamespaces(element, mediator.getBinaryElementXPath());
        }

        return element;
    }

    private OMElement serializeTargetDirectory(WriteBinaryFileMediator mediator) {
        OMElement element = createConfigElement(WriteBinaryFileMediatorConfigConstants.TARGET_DIRECTORY_TAG);

        if (mediator.getTargetDirectory() != null) {
            addValueAttribute(element, mediator.getTargetDirectory());
        } else if (mediator.getTargetDirectoryExpression() != null) {
            addExpressionAttribute(element, mediator.getTargetDirectoryExpression());
        }

        return element;
    }

    private OMElement serializeTargetFileName(WriteBinaryFileMediator mediator) {
        OMElement element = createConfigElement(WriteBinaryFileMediatorConfigConstants.TARGET_FILE_NAME_TAG);

        if (mediator.getTargetFileName() != null) {
            addValueAttribute(element, mediator.getTargetFileName());
        } else if (mediator.getTargetFileNameExpression() != null) {
            addExpressionAttribute(element, mediator.getTargetFileNameExpression());
        }

        return element;
    }

    private OMElement serializeForceUniqueFileName(WriteBinaryFileMediator mediator) {
        if (mediator.getForceUniqueFileName() != null) {
            OMElement element = createConfigElement(WriteBinaryFileMediatorConfigConstants.FORCE_UNIQUE_FILE_NAME_TAG);
            addValueAttribute(element, mediator.getForceUniqueFileName());
            return element;
        }

        return null;
    }

    private OMElement serializeAllowOverwrite(WriteBinaryFileMediator mediator) {
        if (mediator.getAllowOverWrite() != null) {
            OMElement element = createConfigElement(WriteBinaryFileMediatorConfigConstants.ALLOW_OVERWRITE_TAG);
            addValueAttribute(element, mediator.getAllowOverWrite());
            return element;
        }

        return null;
    }


    private void addExpressionAttribute(OMElement element, SynapseXPath expression) {
        element.addAttribute(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_EXPRESSION.getLocalPart(), expression.toString(), null);
        serializeNamespaces(element, expression);
    }

    private void addValueAttribute(OMElement element, String attributeValue) {
        element.addAttribute(WriteBinaryFileMediatorConfigConstants.ATTRIBUTE_VALUE.getLocalPart(), attributeValue, null);
    }

    private OMElement createConfigElement(QName qName) {
        return fac.createOMElement(qName.getLocalPart(), synNS);
    }
}
