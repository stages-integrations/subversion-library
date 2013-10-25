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
package de.shadowhunt.subversion;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Interface listing all available operations on a subversion repository
 */
@ThreadSafe
public interface Repository {

	/**
	 * Save all modifications of the current running {@link Transaction}
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param message the commit message for the expected operation
	 */
	void commit(Transaction transaction, String message);

	/**
	 * Recursively copy a resource in the given revision
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param srcRevision {@link Revision} of the resource to copy
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 */
	void copy(Transaction transaction, Resource srcResource, Revision srcRevision, Resource targetResource);

	/**
	 * Create a folder with all necessary parent folders
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param parent whether to create missing parent folders or not
	 */
	void createFolder(Transaction transaction, Resource resource, boolean parent);

	/**
	 * Create a new {@link Transaction} to make modifications within
	 *
	 * @return the new {@link Transaction}
	 */
	Transaction createTransaction();

	/**
	 * Delete the resource from the repository
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 */
	void delete(Transaction transaction, Resource resource);

	/**
	 * Remove the given properties form the resource
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param properties {@link ResourceProperty} to remove
	 */
	void deleteProperties(Transaction transaction, Resource resource, ResourceProperty... properties);

	/**
	 * Download the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return {@link InputStream} from which the content can be read (caller has to close the stream properly)
	 */
	InputStream download(Resource resource, Revision revision);

	/**
	 * Determine the HTTP download URI for the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return the HTTP download {@link URI} for the resource
	 */
	URI downloadURI(Resource resource, Revision revision);

	/**
	 * Check if the resource already exists in the latest revision of the repository
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 *
	 * @return {@code true} if the resource already exists in the latest revision of the repository otherwise {@code false}
	 */
	boolean exists(Resource resource, Revision revision);

	URI getBaseUri();

	/**
	 * Retrieve information for the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @return {@link de.shadowhunt.subversion.internal.InfoImpl} for the resource
	 */
	Info info(Resource resource, Revision revision);

	/**
	 * Retrieve information for the resource in the given revision and its child resources (depending on depth parameter)
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param revision the {@link Revision} of the resource to retrieve
	 * @param depth whether to retrieve only for the given resource, its children or only part of its children depending on the value of {@link Depth}
	 * @return {@link List} of {@link de.shadowhunt.subversion.internal.InfoImpl} for the resource and its child resources (depending on depth parameter)
	 */
	List<Info> list(Resource resource, Revision revision, Depth depth);

	/**
	 * Mark the expected revision of the resource as locked
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param steal if the resource is locked by another user {@code true} will override the lock, otherwise the operation will fail
	 */
	void lock(Resource resource, boolean steal);

	/**
	 * Retrieve the log information for the revisions between startRevision and endRevision of the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param startRevision the first {@link Revision} of the resource to retrieve (including)
	 * @param endRevision the last {@link Revision} of the resource to retrieve (including)
	 * @param limit maximal number of {@link Log} entries, if the value is lower or equal to {@code 0} all entries will be returned
	 *
	 * @return ordered (early to latest) {@link List} of {@link de.shadowhunt.subversion.internal.LogImpl} for the revisions between startRevision and endRevision of the resource
	 */
	List<Log> log(Resource resource, Revision startRevision, Revision endRevision, int limit);

	/**
	 * Recursively move a resource (latest revision)
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param srcResource the {@link Resource} of the source resource (relative to the repository root)
	 * @param targetResource the {@link Resource} of the target resource (relative to the repository root)
	 */
	void move(Transaction transaction, Resource srcResource, Resource targetResource);

	/**
	 * Abort the current running {@link Transaction} and revert all modifications
	 *
	 * @param transaction the current running {@link Transaction}
	 */
	void rollback(Transaction transaction);

	/**
	 * Set the given properties for the resource (new properties will be added, existing properties will be overridden)
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param properties {@link ResourceProperty} to add or override
	 */
	void setProperties(Transaction transaction, Resource resource, ResourceProperty... properties);

	/**
	 * Remove the lock on the expected revision of the resource
	 *
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param force the user that created the lock must match the user who wants to delete it, unless force is {@code true}
	 */
	void unlock(Resource resource, boolean force);

	/**
	 * Upload a new revision of the resource and set properties
	 *
	 * @param transaction the current running {@link Transaction}
	 * @param resource the {@link Resource} of the resource (relative to the repository root)
	 * @param content {@link InputStream} from which the content will be read (will be closed after transfer)
	 */
	void upload(Transaction transaction, Resource resource, InputStream content);
}
