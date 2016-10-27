# WSO2 ESB Write Binary File Mediator

## What is WSO2 ESB?
[WSO2 ESB](http://wso2.com/products/enterprise-service-bus/) is an open source Enterprise Service Bus that enables interoperability among various heterogeneous systems and business applications.

## Features
This mediator writes the contents of a base64-encoded binary element in message payload to a file in the local file system and replaces the binary content in the element with full path of the file.

E.g. if you have retrieved a BLOB from a database using WSO2 DSS (or a binary file using a VFS proxy), you can store file contents locally using this mediator.

## Usage

### 1. Install the mediator to the ESB
Copy the `wso2-esb-write-binary-file-mediator-X.Y.Z.jar` to `$WSO2_ESB_HOME/repository/components/dropins/`.

### 2. Use it in your proxies/sequences

##### Examples

Minimal example:
```xml
<writeBinaryFile>
   <binaryElementXPath value="//binaryContent"/>
   <targetDirectory value="/tmp"/>
   <targetFileName value="foo.zip"/>
</writeBinaryFile>
```

Full example:
```xml
<writeBinaryFile>
   <binaryElementXPath value="//binaryContent"/>
   <targetDirectory value="/tmp"/>
   <targetFileName value="foo.zip"/>
   <forceUniqueFileName value="false"/>
   <allowOverwrite value="false"/>
</writeBinaryFile>
```

##### Example scenario

Sample payload before mediator:
```xml
<Entries xmlns="http://ws.wso2.org/dataservice">
  <Entry>
    <id>8</id>
    <image>iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAAJcEhZcwAADsMAAA7DAcdvqGQAAAAMSURBVBhXY2BgYAAAAAQAAVzN/2kAAAAASUVORK5CYII=</image>
  </Entry>
</Entries>
```

Using mediator config:
```xml
<writeBinaryFile>
   <binaryElementXPath xmlns:ns1="http://ws.wso2.org/dataservice" value="//ns1:image"/>
   <targetDirectory value="/tmp"/>
   <targetFileName value="temppi.png"/>
</writeBinaryFile>
```

Payload after mediation:
```xml
<Entries xmlns="http://ws.wso2.org/dataservice">
  <Entry>
    <id>8</id>
    <image>/tmp/temppi.png</image>
  </Entry>
</Entries>
```
And the file exists in /tmp/temppi.png

## Input fields

<table>
<thead>
<tr>
    <td>Field</td>
    <td>Value type</td>
    <td>Description</td>
    <td>Required</td>
</tr>
</thead>
<tbody>
<tr>
    <td><b>binaryElementXPath</b></td>
    <td>value</td>
    <td>XPath to the element with the binary content</td>
    <td>Yes</td>
</tr>
<tr>
    <td><b>targetDirectory</b></td>
    <td>value/expression</td>
    <td>The folder where output file is written</td>
    <td>Yes</td>
</tr>
<tr>
    <td><b>targetFileName</b></td>
    <td>value/expression</td>
    <td>Name of the file to be written</td>
    <td>Yes</td>
</tr>
<tr>
    <td><b>forceUniqueFileName<b/></td>
    <td>value</td>
    <td>Whether or not the mediator forces a unique file name for output.<br/><br/>If "true", message context id will be appended to the start of the output file name (e.g. with targetFileName="temppi.png" the actual output file would be something like urn:uuid:e6e188f0-fd9b-4871-af09-8e36733023b5_temppi.png) <br/><br/>Default is "false"</td>
    <td>No</td>
</tr>
<tr>
    <td><b>allowOverwrite</b></td>
    <td>value</td>
    <td>Whether or not BinaryFileMediator is allowed to overwrite an existing file.<br/><br/>Default is "true"</td>
    <td>No</td>
</tr>
</tbody>
</table>

## Technical Requirements

#### Usage

* Oracle Java 6 or above
* WSO2 ESB
    * Wrapper Mediator has been tested with WSO2 ESB versions 4.8.1, 4.9.0 & 5.0.0

#### Development

* Java 6 + Maven 3.0.X

### Contributors

- [Esa Heikkinen](https://github.com/esaheikkinen)
- [Jere Karppinen](https://github.com/jerekarppinen)

## [License](LICENSE)

Copyright &copy; 2016 [Mystes Oy](http://www.mystes.fi). Licensed under the [Apache 2.0 License](LICENSE).