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
package de.shadowhunt.http.client;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpRequest;
import org.apache.http.annotation.Immutable;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;

/**
 * A {@link WebDavHttpRequestRetryHandler} which retires all requested
 * HTTP and DAV methods which should be idempotent according to RFC-2616.
 */
@Immutable
public class WebDavHttpRequestRetryHandler extends DefaultHttpRequestRetryHandler {

	private final Map<String, Boolean> idempotentMethods;

	/**
	 * Create a {@link WebDavHttpRequestRetryHandler} with default settings
	 */
	public WebDavHttpRequestRetryHandler() {
		this(3, true);
	}

	/**
	 * Create a {@link WebDavHttpRequestRetryHandler}
	 *
	 * @param retryCount number of times a method will be retried
	 * @param requestSentRetryEnabled whether or not methods that have successfully sent their request will be retried
	 */
	public WebDavHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
		super(retryCount, requestSentRetryEnabled);

		idempotentMethods = new HashMap<String, Boolean>();
		// http
		idempotentMethods.put("DELETE", Boolean.TRUE);
		idempotentMethods.put("GET", Boolean.TRUE);
		idempotentMethods.put("HEAD", Boolean.TRUE);
		idempotentMethods.put("OPTIONS", Boolean.TRUE);
		idempotentMethods.put("PUT", Boolean.TRUE);
		idempotentMethods.put("POST", Boolean.FALSE); // not idempotent FIXME in subversion context idempotent?
		idempotentMethods.put("TRACE", Boolean.TRUE);
		// webdav
		idempotentMethods.put("CHECKOUT", Boolean.TRUE);
		idempotentMethods.put("COPY", Boolean.TRUE);
		idempotentMethods.put("LOCK", Boolean.TRUE);
		idempotentMethods.put("MERGE", Boolean.TRUE);
		idempotentMethods.put("MKACTIVITY", Boolean.TRUE);
		idempotentMethods.put("MKCOL", Boolean.FALSE); // not idempotent
		idempotentMethods.put("PROPFIND", Boolean.TRUE);
		idempotentMethods.put("PROPPATCH", Boolean.TRUE);
		idempotentMethods.put("REPORT", Boolean.TRUE);
		idempotentMethods.put("UNLOCK", Boolean.TRUE);
	}

	@Override
	protected boolean handleAsIdempotent(final HttpRequest request) {
		final String method = request.getRequestLine().getMethod().toUpperCase(Locale.US);
		final Boolean b = idempotentMethods.get(method);
		return (b != null) && b;
	}
}
