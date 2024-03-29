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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

import de.methodpark.subversion.TransmissionException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

class DownloadOperation extends AbstractOperation<Optional<InputStream>> {

    private final QualifiedResource resource;

    DownloadOperation(final URI repository, final QualifiedResource resource) {
        super(repository);
        this.resource = resource;
    }

    @Override
    protected HttpUriRequest createRequest() {
        final URI uri = URIUtils.appendResources(repository, resource);
        return new DavTemplateRequest("GET", uri);
    }

    @Override
    public Optional<InputStream> execute(final HttpClient client, final HttpContext context) {
        final HttpUriRequest request = createRequest();

        try {
            final HttpResponse response = client.execute(request, context);
            if (getStatusCode(response) == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            }
            final InputStream content = getContent(response);
            check(response);
            return Optional.of(content);
        } catch (final IOException e) {
            throw new TransmissionException(e);
        }
    }

    @Override
    public Optional<InputStream> handleResponse(final HttpResponse response) {
        return processResponse(response);
    }

    @Override
    protected boolean isExpectedStatusCode(final int statusCode) {
        return (HttpStatus.SC_OK == statusCode) || (HttpStatus.SC_NOT_FOUND == statusCode);
    }

    @Override
    protected Optional<InputStream> processResponse(final HttpResponse response) {
        // we return the content stream
        throw new UnsupportedOperationException();
    }
}
