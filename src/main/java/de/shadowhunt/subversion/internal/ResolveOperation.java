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

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.Revision;
import de.shadowhunt.subversion.internal.AbstractBaseRepository.ResourceMapper;
import de.shadowhunt.subversion.internal.util.URIUtils;

class ResolveOperation extends AbstractOperation<Resource> {

	private final ResourceMapper config;

	private final Revision expected;

	private final boolean reportNonExistingResources;

	private final Resource resource;

	private final Revision revision;

	ResolveOperation(final URI repository, final Resource resource, final Revision revision, final Revision expected, final ResourceMapper config, final boolean reportNonExistingResources) {
		super(repository);

		Validate.notNull(resource, "resource must not be null");
		Validate.notNull(revision, "revision must not be null");
		Validate.notNull(expected, "excepted must not be null");
		Validate.notNull(config, "config must not be null");

		this.resource = resource;
		this.revision = revision;
		this.expected = expected;
		this.config = config;
		this.reportNonExistingResources = reportNonExistingResources;
	}

	@Override
	protected HttpUriRequest createRequest() {
		final URI uri = URIUtils.createURI(repository, resource);
		final DavTemplateRequest request = new DavTemplateRequest("REPORT", uri);

		final StringBuilder sb = new StringBuilder(XML_PREAMBLE);
		sb.append("<get-locations xmlns=\"svn:\"><path/><peg-revision>");
		sb.append(revision);
		sb.append("</peg-revision><location-revision>");
		sb.append(expected);
		sb.append("</location-revision></get-locations>");

		request.setEntity(new StringEntity(sb.toString(), CONTENT_TYPE_XML));
		return request;
	}

	@Override
	protected boolean isExpectedStatusCode(final int statusCode) {
		return (HttpStatus.SC_OK == statusCode)
				|| (!reportNonExistingResources && (HttpStatus.SC_NOT_FOUND == statusCode));
	}

	@Override
	protected Resource processResponse(final HttpResponse response) {
		Validate.notNull(response, "response must not be null");

		if (!reportNonExistingResources) {
			final int statusCode = getStatusCode(response);
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				EntityUtils.consumeQuietly(response.getEntity());
				return null;
			}
		}

		final InputStream in = getContent(response);
		try {
			final Resolve resolve = Resolve.read(in);
			return config.getVersionedResource(resolve.getResource(), expected);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
