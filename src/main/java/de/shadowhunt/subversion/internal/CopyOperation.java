/*
 * #%L
 * Shadowhunt Subversion
 * %%
 * Copyright (C) 2013 shadowhunt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.shadowhunt.subversion.internal;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.http.client.methods.DavTemplateRequest;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.util.URIUtils;

public class CopyOperation extends AbstractVoidOperation {

	private final Resource source;

	private final Resource target;

	public CopyOperation(final URI repository, final Resource source, final Resource target) {
		super(repository);
		this.source = source;
		this.target = target;
	}

	@Override
	protected void checkResponse(final HttpResponse response) {
		check(response, HttpStatus.SC_CREATED);
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI sourceUri = URIUtils.createURI(repository, source);
		final URI targetUri = URIUtils.createURI(repository, target);
		final DavTemplateRequest request = new DavTemplateRequest("COPY", sourceUri);
		request.addHeader("Destination", targetUri.toASCIIString());
		request.addHeader("Depth", Depth.INFINITY.value);
		request.addHeader("Override", "T");
		return request;
	}
}
