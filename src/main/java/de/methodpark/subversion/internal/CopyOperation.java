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

import de.methodpark.subversion.Depth;
import de.methodpark.subversion.LockToken;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

class CopyOperation extends AbstractVoidOperation {

    private final Optional<LockToken> lockToken;

    private final QualifiedResource source;

    private final QualifiedResource target;

    CopyOperation(final URI repository, final QualifiedResource source, final QualifiedResource target, final Optional<LockToken> lockToken) {
        super(repository);
        this.source = source;
        this.target = target;
        this.lockToken = lockToken;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI sourceUri = URIUtils.appendResources(repository, source);
        final URI targetUri = URIUtils.appendResources(repository, target);
        final DavTemplateRequest request = new DavTemplateRequest("COPY", sourceUri);
        request.addHeader("Destination", targetUri.toASCIIString());
        request.addHeader("Depth", Depth.INFINITY.value);
        request.addHeader("Override", "T");

        lockToken.ifPresent(x -> request.addHeader("If", "<" + targetUri + "> (<" + x + ">)"));

        return request;
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_CREATED == statusCode) || (HttpStatus.SC_NO_CONTENT == statusCode);
    }
}
