/**
 * Copyright (C) 2013 shadowhunt (dev@shadowhunt.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.shadowhunt.subversion.internal;

import java.net.URI;
import java.util.Optional;

import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

class ExistsOperation extends AbstractPropfindOperation<Boolean> {

    ExistsOperation(final URI repository, final Resource resource, final Resource marker) {
        super(repository, resource, marker, Depth.EMPTY, Optional.<ResourceProperty.Key[]>empty());
    }

    @Override
    protected Boolean processResponse(final HttpResponse response) {
        final int statusCode = getStatusCode(response);
        return (statusCode == HttpStatus.SC_MULTI_STATUS);
    }

}
