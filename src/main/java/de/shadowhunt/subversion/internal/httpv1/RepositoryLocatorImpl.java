package de.shadowhunt.subversion.internal.httpv1;

import java.net.URI;

import org.apache.http.client.HttpClient;
import org.apache.http.protocol.HttpContext;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.internal.RepositoryLocator;

public class RepositoryLocatorImpl implements RepositoryLocator {

	@Override
	public boolean isSupported(final Repository.ProtocolVersion version) {
		return Repository.ProtocolVersion.HTTPv1 == version;
	}

	@Override
	public Repository create(final URI repository, final Resource prefix, final HttpClient client, final HttpContext context) {
		return new RepositoryImpl(repository, prefix, client, context);
	}
}