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
package de.methodpark.subversion.internal;

import java.net.URI;
import java.util.Optional;

import de.methodpark.subversion.LockToken;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class DeleteOperation extends AbstractVoidOperation {

    private final Optional<LockToken> lockToken;

    private final QualifiedResource resource;

    DeleteOperation(final URI repository, final QualifiedResource resource, final Optional<LockToken> lockToken) {
        super(repository);
        this.resource = resource;
        this.lockToken = lockToken;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        final HttpUriRequest request = new DavTemplateRequest("DELETE", uri);

        lockToken.ifPresent(x -> request.addHeader("If", "<" + uri + "> (<" + x + ">)"));

        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return HttpStatus.SC_NO_CONTENT == statusCode;
    }
}
