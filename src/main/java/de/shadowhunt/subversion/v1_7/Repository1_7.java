package de.shadowhunt.subversion.v1_7;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;

import de.shadowhunt.subversion.AbstractRepository;
import de.shadowhunt.subversion.Depth;
import de.shadowhunt.subversion.InfoEntry;
import de.shadowhunt.subversion.Path;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;

/**
 * {@link Repository1_7} supports subversion servers of version 1.7.X
 */
public class Repository1_7 extends AbstractRepository<RequestFactory1_7> {

	protected static final String PREFIX_ME = "/!svn/me";

	protected static final String PREFIX_RVR = "/!svn/rvr/";

	protected static final String PREFIX_TXN = "/!svn/txn/";

	protected static final String PREFIX_TXR = "/!svn/txr/";

	protected Repository1_7(final URI repositoryRoot, final boolean trustServerCertificat) {
		super(repositoryRoot, trustServerCertificat, new RequestFactory1_7());
	}

	protected void contentUpload(final Path resource, final InfoEntry info, final String uuid, @Nullable final InputStream content) {
		if (content == null) {
			return;
		}

		if (!isAuthenticated()) {
			triggerAuthentication();
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createUploadRequest(uri, info.getLockToken(), resourceUri, content);
		execute(request, HttpStatus.SC_CREATED, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void copy(final Path srcResource, final Revision srcRevision, final Path targetResource, final String message) {
		final InfoEntry info = info(srcResource, srcRevision, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		createMissingFolders(PREFIX_TXR, uuid, targetResource.getParent());
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		merge(info, uuid);
	}

	protected void copy0(final Path srcResource, final Revision srcRevision, final Path targetResource, final String uuid) {
		final URI src = URI.create(repository + PREFIX_RVR + srcRevision + srcResource.getValue());
		final URI target = URI.create(repository + PREFIX_TXR + uuid + targetResource.getValue());
		final HttpUriRequest request = requestFactory.createCopyRequest(src, target);
		execute(request, HttpStatus.SC_CREATED);
	}

	@Override
	public void createFolder(final Path resource, final String message) {
		if (exists(resource, Revision.HEAD)) {
			return;
		}

		final String uuid = prepareTransaction();
		final Path infoResource = createMissingFolders(PREFIX_TXR, uuid, resource);
		final InfoEntry info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		merge(info, uuid);
	}

	@Override
	public void delete(final Path resource, final String message) {
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		delete0(resource, uuid);
		final InfoEntry info = info(resource, Revision.HEAD, false);
		merge(info, uuid);
	}

	protected void delete0(final Path resource, final String uuid) {
		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final HttpUriRequest request = requestFactory.createDeleteRequest(uri);
		execute(request, HttpStatus.SC_NO_CONTENT);
	}

	@Override
	public void deleteProperties(final Path resource, final String message, final ResourceProperty... properties) {
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		final InfoEntry info = info(resource, Revision.HEAD, false);
		propertiesRemove(resource, info, uuid, properties);
		merge(info, uuid);
	}

	@Override
	public URI downloadURI(final Path resource, final Revision revision) {
		if (Revision.HEAD.equals(revision)) {
			return URI.create(repository + resource.getValue());
		}
		return URI.create(repository + PREFIX_RVR + revision + resource.getValue());
	}

	@Override
	public List<InfoEntry> list(final Path resource, final Revision revision, final Depth depth, final boolean withCustomProperties) {
		final Revision concreateRevision = getConcreateRevision(resource, revision);
		final String uriPrefix = repository + PREFIX_RVR + concreateRevision;
		return list(uriPrefix, resource, depth, withCustomProperties);
	}

	protected void merge(final InfoEntry info, final String uuid) {
		final Path path = Path.create(repository.getPath() + PREFIX_TXN + uuid);
		final HttpUriRequest request = requestFactory.createMergeRequest(repository, path, info);
		execute(request, HttpStatus.SC_OK);
	}

	@Override
	public void move(final Path srcResource, final Path targetResource, final String message) {
		final InfoEntry info = info(srcResource, Revision.HEAD, false);
		final String uuid = prepareTransaction();
		setCommitMessage(uuid, message);
		copy0(srcResource, info.getRevision(), targetResource, uuid);
		delete0(srcResource, uuid);
		merge(info, uuid);
	}

	protected String prepareTransaction() {
		final URI uri = URI.create(repository + PREFIX_ME);

		final HttpUriRequest request = requestFactory.createPrepareRequest(uri);
		final HttpResponse response = execute(request, HttpStatus.SC_CREATED);

		return response.getFirstHeader("SVN-Txn-Name").getValue();
	}

	protected void propertiesRemove(final Path resource, final InfoEntry info, final String uuid, final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createRemovePropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void propertiesSet(final Path resource, final InfoEntry info, final String uuid, @Nullable final ResourceProperty... properties) {
		final ResourceProperty[] filtered = ResourceProperty.filteroutSystemProperties(properties);
		if (filtered.length == 0) {
			return;
		}

		final URI uri = URI.create(repository + PREFIX_TXR + uuid + resource.getValue());
		final URI resourceUri = URI.create(repository + resource.getValue());

		final HttpUriRequest request = requestFactory.createSetPropertiesRequest(uri, info.getLockToken(), resourceUri, filtered);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	protected void setCommitMessage(final String uuid, final String message) {
		final URI uri = URI.create(repository + PREFIX_TXN + uuid);

		final String trimmedMessage = StringUtils.trimToEmpty(message);
		final HttpUriRequest request = requestFactory.createCommitMessageRequest(uri, trimmedMessage);
		execute(request, HttpStatus.SC_MULTI_STATUS);
	}

	@Override
	protected void upload0(final Path resource, final String message, @Nullable final InputStream content, @Nullable final ResourceProperty... properties) {
		final String uuid = prepareTransaction();

		final Path infoResource;
		if (exists(resource, Revision.HEAD)) {
			infoResource = resource;
		} else {
			infoResource = createMissingFolders(PREFIX_TXR, uuid, resource);
		}
		final InfoEntry info = info(infoResource, Revision.HEAD, false);
		setCommitMessage(uuid, message);
		contentUpload(resource, info, uuid, content);
		propertiesSet(resource, info, uuid, properties);
		merge(info, uuid);
	}
}