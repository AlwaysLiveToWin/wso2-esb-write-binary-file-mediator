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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import javax.activation.DataHandler;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * BinaryFileMediator writes the contents of an XML element containing
 * base64-encoded binary data to a file specified in mediator configuration
 * and replaces the binary data in the payload with the absolute path
 * of the output file.
 * <p/>
 * Detailed instructions available in
 * <a href="https://mystes.jira.com/wiki/display/COMLIB/BinaryFileMediator">Mystes Wiki</a>
 */
public class WriteBinaryFileMediator extends AbstractMediator {

    private static final boolean DEFAULT_FORCE_UNIQUE_FILE_NAME = false;
    private static final boolean DEFAULT_ALLOW_OVERWRITE = true;

    // binaryElementXPath can only be configured in 'value' attribute, but
    // stored here as SynapseXPath (as opposed to String) to be able to include
    // namespace definitions in XPath definition
    private SynapseXPath binaryElementXPath;

    private String targetDirectory;
    private SynapseXPath targetDirectoryExpression;
    private String targetFileName;
    private SynapseXPath targetFileNameExpression;
    private String forceUniqueFileName;
    private String allowOverWrite;


    @Override
    public boolean mediate(MessageContext messageContext) {
        validateState(messageContext);

        Object node = findNodeWithBinaryContent(messageContext);
        String fullPath = writeNodeContentsToFile(node, messageContext);

        if (fullPath != null) {
            replaceBinaryElementContent(node, fullPath);
        }

        return true;
    }

    public SynapseXPath getBinaryElementXPath() {
        return binaryElementXPath;
    }

    public void setBinaryElementXPath(SynapseXPath xPath) {
        this.binaryElementXPath = xPath;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(String targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

    public SynapseXPath getTargetDirectoryExpression() {
        return targetDirectoryExpression;
    }

    public void setTargetDirectoryExpression(SynapseXPath targetDirectoryExpression) {
        this.targetDirectoryExpression = targetDirectoryExpression;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public SynapseXPath getTargetFileNameExpression() {
        return targetFileNameExpression;
    }

    public void setTargetFileNameExpression(SynapseXPath targetFileNameExpression) {
        this.targetFileNameExpression = targetFileNameExpression;
    }

    public String getForceUniqueFileName() {
        return forceUniqueFileName;
    }

    private boolean isForceUniqueFileName() {
        return getForceUniqueFileName() == null ? DEFAULT_FORCE_UNIQUE_FILE_NAME : Boolean.valueOf(getForceUniqueFileName());
    }

    public void setForceUniqueFileName(String forceUniqueFileName) {
        this.forceUniqueFileName = forceUniqueFileName;
    }

    public String getAllowOverWrite() {
        return allowOverWrite;
    }

    private boolean isAllowOverWrite() {
        return getAllowOverWrite() == null ? DEFAULT_ALLOW_OVERWRITE : Boolean.valueOf(getAllowOverWrite());
    }

    public void setAllowOverWrite(String allowOverWrite) {
        this.allowOverWrite = allowOverWrite;
    }

    private void replaceBinaryElementContent(Object node, String fullPath) {
        OMElement element = digIntoOmElement(node);
        element.setText(fullPath);
    }

    private Object findNodeWithBinaryContent(MessageContext messageContext) {
        OMElement payload = messageContext.getEnvelope().getBody().getFirstElement();
        try {
            AXIOMXPath xpath = getBinaryElementXPath();
            xpath.addNamespaces(payload);
            Object node = xpath.selectSingleNode(payload);
            if (node == null) {
                handleException("Binary content not found in payload using xpath " + getBinaryElementXPath(), messageContext);
            }
            return node;
        } catch (JaxenException e) {
            handleException("Invalid source element XPath specified in mediator configuration", e, messageContext);
            return null;
        }
    }

    private String writeNodeContentsToFile(Object node, MessageContext messageContext) {
        OMText text = digIntoOmText(node, messageContext);

        if (text != null) {
            text.setBinary(true);

            return writeOmTextToFile(messageContext, text);
        } else {
            log.info("No content found in binary element " + getBinaryElementXPath() + ", not writing output file");
            return null;
        }
    }

    private String writeOmTextToFile(MessageContext messageContext, OMText text) {
        final String targetFilePath = resolveOutputFilePath(messageContext);
        FileOutputStream out = null;
        try {
            File targetFile = new File(targetFilePath);
            if (!isAllowOverWrite() && targetFile.exists()) {
                log.warn("File " + targetFilePath + " exists, refusing to overwrite it as overwriting is disabled in configuration");
            } else {
                out = new FileOutputStream(targetFile);
                Object dataHandler = text.getDataHandler();
                if (dataHandler == null) {
                    handleException("Unable to extract DataHandler from OMText", messageContext);
                }
                ((DataHandler) dataHandler).writeTo(out);
                return targetFilePath;
            }
        } catch (IOException e) {
            handleException("Error while writing output file " + targetFilePath, e, messageContext);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    private String resolveOutputFilePath(MessageContext messageContext) {
        String fileName = resolveTargetFileName(messageContext);

        if (isForceUniqueFileName()) {
            fileName = messageContext.getMessageID() + "_" + fileName;
        }

        return resolveTargetDirectory(messageContext) + File.separator + fileName;
    }

    private String resolveTargetFileName(MessageContext messageContext) {
        if (getTargetFileName() != null) {
            return getTargetFileName();
        }
        SynapseXPath targetFileNameXPath = getTargetFileNameExpression();
        try {
            String fileName = digIntoTextValue(targetFileNameXPath, messageContext);
            if (fileName == null) {
                handleException("Unable to determine target file name using XPath " + targetFileNameXPath, messageContext);
            }
            return fileName;
        } catch (JaxenException e) {
            handleException("Error while evaluating target file name XPath " + targetFileNameXPath, e, messageContext);
            return null;
        }
    }

    private String resolveTargetDirectory(MessageContext messageContext) {
        if (getTargetDirectory() != null) {
            return getTargetDirectory();
        }
        SynapseXPath targetDirectoryXPath = getTargetDirectoryExpression();
        try {
            String directory = digIntoTextValue(targetDirectoryXPath, messageContext);
            if (directory == null) {
                handleException("Unable to determine target directory using XPath " + targetDirectoryXPath, messageContext);
            }
            return directory;
        } catch (JaxenException e) {
            handleException("Error while evaluating target directory XPath " + targetDirectoryXPath, e, messageContext);
            return null;
        }
    }

    private String digIntoTextValue(SynapseXPath xPath, MessageContext messageContext) throws JaxenException {
        Object evaluationResult = xPath.evaluate(messageContext);

        return extractTextValue(xPath, messageContext, evaluationResult);
    }

    private String extractTextValue(SynapseXPath xPath, MessageContext messageContext, Object evaluationResult) throws JaxenException {
        if (evaluationResult instanceof String) {
            return (String) evaluationResult;
        } else if (evaluationResult instanceof OMElement) {
            return ((OMElement) evaluationResult).getText();
        } else if (evaluationResult instanceof OMAttribute) {
            return ((OMAttribute) evaluationResult).getAttributeValue();
        } else if (evaluationResult instanceof OMText) {
            return ((OMText) evaluationResult).getText();
        } else if (evaluationResult != null) {
            if (evaluationResult instanceof List) {
                List<?> resultList = (List<?>) evaluationResult;
                if (resultList.size() > 1) {
                    handleException("More than one result found with xpath " + xPath + ", refusing to proceed.", messageContext);
                } else {
                    if (!resultList.isEmpty()) {
                        return extractTextValue(xPath, messageContext, resultList.get(0));
                    }
                }
            } else {
                handleException("Unsupported result type for " + xPath + ": " + evaluationResult.getClass().getCanonicalName(), messageContext);
                return null;
            }

        }
        getLog(messageContext).traceOrDebug("Resolving XPath " + xPath + " resulted in null");
        return null;
    }

    private OMText digIntoOmText(Object node, MessageContext messageContext) {
        if (node instanceof OMText) {
            return (OMText) node;
        } else if (node instanceof OMElement) {
            OMNode omNode = ((OMElement) node).getFirstOMChild();
            if (omNode == null) {
                // element found but it is empty - not an error, but output file will not be written
                return null;
            } else if (omNode instanceof OMText) {
                return (OMText) omNode;
            }
        }
        handleException("Binary content not found from element with XPath " + getBinaryElementXPath(), messageContext);
        return null;
    }

    private OMElement digIntoOmElement(Object node) {
        if (node instanceof OMElement) {
            return (OMElement) node;
        } else {
            return (OMElement) ((OMText) node).getParent();
        }
    }

    private void validateState(MessageContext messageContext) {
        if (getBinaryElementXPath() == null) {
            handleException("Binary element XPath not specified in mediator configuration", messageContext);
        }
        if (getTargetDirectory() == null && getTargetDirectoryExpression() == null) {
            handleException("Target directory not specified in mediator configuration", messageContext);
        }
        if (getTargetFileName() == null && getTargetFileNameExpression() == null) {
            handleException("Target file name not specified in mediator configuration", messageContext);
        }
    }

}
