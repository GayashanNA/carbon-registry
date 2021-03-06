/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.metadata.provider.version;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.metadata.Base;
import org.wso2.carbon.registry.metadata.Constants;
import org.wso2.carbon.registry.metadata.provider.MetadataProvider;
import org.wso2.carbon.registry.metadata.provider.util.Util;
import org.wso2.carbon.registry.metadata.service.HTTPServiceV1;
import org.wso2.carbon.registry.metadata.version.HTTPServiceVersionV1;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HTTPServiceVersionProviderV1 implements MetadataProvider {

    private static final Log log = LogFactory.getLog(HTTPServiceVersionProviderV1.class);

    @Override
    public Resource buildResource(Base metadata, Resource resource) throws RegistryException {
        OMElement root = Util.getContentRoot();
        OMElement attributes = Util.getAttributeRoot();
        OMElement properties = Util.getPropertyRoot();

        createAttributesContent((HTTPServiceVersionV1) metadata, attributes);
        createPropertiesContent((HTTPServiceVersionV1) metadata, properties);
        root.addChild(properties);
        root.addChild(attributes);
        try {
            String content = root.toStringWithConsume();
            resource.setContent(content);
            resource.setMediaType(metadata.getMediaType());
            resource.setUUID(metadata.getUUID());
        } catch (XMLStreamException e) {
            log.error("Xml stream exception occurred while building resource content " + e.getMessage());
            throw new RegistryException("Xml stream exception occurred while building resource content", e);
        }
        return resource;
    }

    @Override
    public Resource updateResource(Base newMetadata, Resource resource) throws RegistryException {
        return buildResource(newMetadata, resource);
    }

    @Override
    public Base get(Resource resource,Registry registry) throws RegistryException {
        try {
            byte[] contentBytes = (byte[]) resource.getContent();
            OMElement root = Util.buildOMElement(contentBytes);
            Map<String, String> propBag = Util.getPropertyBag(root);
            return getFilledBean(root, propBag, registry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private HTTPServiceVersionV1 getFilledBean(OMElement root, Map<String, String> propBag, Registry registry) throws RegistryException {
        Map<String, String> attributeMap = new HashMap<String, String>();
        OMElement properties = root.getFirstChildWithName(new QName(Constants.CONTENT_ATTRIBUTE_EL_ROOT_NAME));
        String uuid = properties.getFirstChildWithName(new QName("uuid")).getText();
        String name = properties.getFirstChildWithName(new QName("name")).getText();
        String baseName = properties.getFirstChildWithName(new QName("baseName")).getText();
        String baseUUID = properties.getFirstChildWithName(new QName("baseUUID")).getText();


        Iterator itr = properties.getChildren();
        while (itr.hasNext()) {
            OMElement el = (OMElement) itr.next();
            String key = el.getLocalName();
            String value = el.getText();
            attributeMap.put(key, value);
        }
        HTTPServiceVersionV1 s = new HTTPServiceVersionV1(registry,name,uuid,baseName,baseUUID,propBag,attributeMap);
        return s;
    }


    private void createAttributesContent(HTTPServiceVersionV1 serviceV1, OMElement element) throws RegistryException {

        OMFactory factory = OMAbstractFactory.getOMFactory();

        OMElement uuid = factory.createOMElement(new QName("uuid"));
        uuid.setText(serviceV1.getUUID());

        OMElement name = factory.createOMElement(new QName("name"));
        name.setText(serviceV1.getName());

        OMElement baseName = factory.createOMElement(new QName("baseName"));
        baseName.setText(serviceV1.getBaseName());

        OMElement baseUUID = factory.createOMElement(new QName("baseUUID"));
        baseUUID.setText(serviceV1.getBaseUUID());

        OMElement mediaType = factory.createOMElement(new QName("mediaType"));
        mediaType.setText(serviceV1.getMediaType());

        OMElement endpointUrl = factory.createOMElement(new QName("endpointUrl"));
        endpointUrl.setText(serviceV1.getEndpointUrl());

        element.addChild(uuid);
        element.addChild(name);
        element.addChild(baseName);
        element.addChild(baseUUID);
        element.addChild(mediaType);
        element.addChild(endpointUrl);


    }

    private void createPropertiesContent(HTTPServiceVersionV1 serviceV1, OMElement element) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        for (Map.Entry<String, String> entry : serviceV1.getPropertyBag().entrySet()) {
            OMElement attribute = factory.createOMElement(new QName(entry.getKey()));
            attribute.setText(entry.getValue());
            element.addChild(attribute);
        }

    }

}
