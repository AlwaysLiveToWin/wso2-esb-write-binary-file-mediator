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

import org.apache.synapse.config.xml.XMLConfigConstants;

import javax.xml.namespace.QName;

/**
 * Created by esa on 10.2.2015.
 */
public final class WriteBinaryFileMediatorConfigConstants {
    public static final String NAMESPACE_STRING = XMLConfigConstants.SYNAPSE_NAMESPACE;
    public static final String ROOT_TAG_NAME = "writeBinaryFile";
    public static final QName ATTRIBUTE_VALUE = new QName(null, "value");
    public static final QName ATTRIBUTE_EXPRESSION = new QName(null, "expression");
    public static final QName ROOT_TAG = new QName(NAMESPACE_STRING, ROOT_TAG_NAME);
    public static final QName BINARY_ELEMENT_XPATH_TAG = new QName(NAMESPACE_STRING, "binaryElementXPath");
    public static final QName TARGET_DIRECTORY_TAG = new QName(NAMESPACE_STRING, "targetDirectory");
    public static final QName TARGET_FILE_NAME_TAG = new QName(NAMESPACE_STRING, "targetFileName");
    public static final QName FORCE_UNIQUE_FILE_NAME_TAG = new QName(NAMESPACE_STRING, "forceUniqueFileName");
    public static final QName ALLOW_OVERWRITE_TAG = new QName(NAMESPACE_STRING, "allowOverwrite");

    private WriteBinaryFileMediatorConfigConstants() {
        // suppress default constructor as class contains constant definitions only
    }
}
