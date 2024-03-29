/**
 * Copyright © 2013-2018 shadowhunt (dev@shadowhunt.de)
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
package de.methodpark.subversion.internal.httpv1;

import java.io.Writer;
import java.net.URI;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.methodpark.subversion.SubversionException;
import de.methodpark.subversion.internal.AbstractVoidOperation;
import de.methodpark.subversion.internal.QualifiedResource;
import de.methodpark.subversion.internal.URIUtils;
import de.methodpark.subversion.internal.XmlConstants;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;

class CheckoutOperation extends AbstractVoidOperation {

    private final QualifiedResource resource;

    private final QualifiedResource transaction;

    CheckoutOperation(final URI repository, final QualifiedResource resource, final QualifiedResource transaction) {
        super(repository);
        this.resource = resource;
        this.transaction = transaction;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final Writer body = new StringBuilderWriter();
        try {
            final XMLStreamWriter writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(body);
            writer.writeStartDocument(XmlConstants.ENCODING, XmlConstants.VERSION_1_0);
            writer.writeStartElement("checkout");
            writer.writeDefaultNamespace(XmlConstants.DAV_NAMESPACE);
            writer.writeStartElement("activity-set");
            writer.writeStartElement("href");
            final URI transactionURI = URIUtils.appendResources(repository, transaction);
            writer.writeCData(transactionURI.toString());
            writer.writeEndElement(); // href
            writer.writeEndElement(); // activity-set
            writer.writeEmptyElement("apply-to-version");
            writer.writeEndElement(); // checkout
            writer.writeEndDocument();
            writer.close();
        } catch (final XMLStreamException e) {
            throw new SubversionException("could not create request body", e);
        }

        final URI uri = URIUtils.appendResources(repository, resource);
        final DavTemplateRequest request = new DavTemplateRequest("CHECKOUT", uri);
        request.setEntity(new StringEntity(body.toString(), CONTENT_TYPE_XML));
        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_CREATED == statusCode;
    }
}
