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

import java.net.URI;
import java.util.UUID;

import de.methodpark.subversion.Resource;
import de.methodpark.subversion.Revision;
import de.methodpark.subversion.internal.AbstractOperation;
import de.methodpark.subversion.internal.QualifiedResource;
import de.methodpark.subversion.internal.TransactionImpl;
import de.methodpark.subversion.internal.URIUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class CreateTransactionOperation extends AbstractOperation<TransactionImpl> {

    private final Revision headRevision;

    private final UUID repositoryId;

    private final QualifiedResource resource;

    private final UUID transactionId = UUID.randomUUID();

    CreateTransactionOperation(final URI repository, final UUID repositoryId, final QualifiedResource resource, final Revision headRevision) {
        super(repository);
        this.repositoryId = repositoryId;
        this.resource = resource;
        this.headRevision = headRevision;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource.getBase(), resource.getResource(), Resource.create(transactionId.toString()));
        return new DavTemplateRequest("MKACTIVITY", uri);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_CREATED == statusCode;
    }

    @Override
    protected TransactionImpl processResponse(final HttpResponse response) {
        return new TransactionImpl(transactionId.toString(), repositoryId, headRevision);
    }
}
