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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;

import org.xml.sax.Attributes;

import de.shadowhunt.subversion.ResourceProperty.Type;

/**
 * Container that holds all status information for a single revision of a resource
 */
public final class InfoEntry {

	static class SubversionInfoHandler extends BasicHandler {

		private boolean checkedin = false;

		private InfoEntry current = null;

		private List<ResourceProperty> customProperties;

		private final boolean includeDirectories;

		private final List<InfoEntry> infos = new ArrayList<InfoEntry>();

		private boolean locktoken = false;

		private boolean resourceType = false;

		private final boolean withCustomProperties;

		SubversionInfoHandler(final boolean withCustomProperties, final boolean includeDirectories) {
			super();
			this.withCustomProperties = withCustomProperties;
			this.includeDirectories = includeDirectories;
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			final String name = getNameFromQName(qName);

			if (current == null) {
				return;
			}

			if ("response".equals(name)) {
				if (withCustomProperties) {
					current.setCustomProperties(customProperties.toArray(new ResourceProperty[customProperties.size()]));
					customProperties = null;
				}

				infos.add(current);
				current = null;
				return;
			}

			if ("baseline-relative-path".equals(name)) {
				final Resource resource = Resource.create(getText());
				current.setResource(resource);
				return;
			}

			if (resourceType && "collection".equals(name)) {
				if (!includeDirectories) {
					// we don't want to include directories in our result list
					current = null;
					return;
				}

				current.setDirectory(true);
				resourceType = false;
				return;
			}

			if (checkedin && "href".equals(name)) {
				final String text = getText();
				final String[] parts = text.split("/");
				final int version = Integer.parseInt(parts[3 + 2]); // prefix + $svn + bc/vrv + VERSION);

				current.setRevision(Revision.create(version));
				checkedin = false;
				return;
			}

			if (locktoken && "href".equals(name)) {
				current.setLockToken(getText());
				locktoken = false;
				return;
			}

			if ("md5-checksum".equals(name)) {
				current.setMd5(getText());
				return;
			}

			if ("repository-uuid".equals(name)) {
				current.setRepositoryUuid(getText());
				return;
			}

			if (!withCustomProperties) {
				return;
			}

			final String namespace = getNamespaceFromQName(qName);
			if ("C".equals(namespace)) {
				final ResourceProperty property = new ResourceProperty(Type.CUSTOM, name, getText());
				customProperties.add(property);
			}
		}

		List<InfoEntry> getInfos() {
			return infos;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
			clearText();

			final String name = getNameFromQName(qName);

			if ("checked-in".equals(name)) {
				checkedin = true;
				return;
			}

			if ("response".equals(name)) {
				current = new InfoEntry();
				locktoken = false;
				resourceType = false;

				if (withCustomProperties) {
					customProperties = new ArrayList<ResourceProperty>();
				}
				return;
			}

			if ("locktoken".equals(name)) {
				locktoken = true;
				return;
			}

			if ("resourcetype".equals(name)) {
				resourceType = true;
				return;
			}
		}
	}

	private static final ResourceProperty[] EMPTY = new ResourceProperty[0];

	/**
	 * {@link Comparator} orders {@link InfoEntry}s by their relative {@link Resource}
	 */
	public static final Comparator<InfoEntry> RESOURCE_COMPARATOR = new Comparator<InfoEntry>() {

		@Override
		public int compare(final InfoEntry si1, final InfoEntry si2) {
			return si1.getResource().compareTo(si2.getResource());
		}
	};

	/**
	 * Reads status information for a single revision of a resource from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @param withCustomProperties whether to read user defined properties
	 * @return {@link InfoEntry} for the resource
	 */
	public static InfoEntry read(final InputStream in, final boolean withCustomProperties) {
		final List<InfoEntry> infos = readList(in, withCustomProperties, true);
		if (infos.isEmpty()) {
			throw new SubversionException("could not find any SubversionInfo in input");
		}
		return infos.get(0);
	}

	/**
	 * Reads a {@link List} of status information for a single revision of various resources from the given {@link InputStream}
	 * @param in {@link InputStream} from which the status information is read (Note: will not be closed)
	 * @param withCustomProperties whether to read user defined properties
	 * @param includeDirectories whether directory resources shall be included in the result
	 * @return {@link InfoEntry} for the resources
	 */
	public static List<InfoEntry> readList(final InputStream in, final boolean withCustomProperties, final boolean includeDirectories) {
		try {
			final SAXParser saxParser = BasicHandler.FACTORY.newSAXParser();
			final SubversionInfoHandler handler = new SubversionInfoHandler(withCustomProperties, includeDirectories);

			saxParser.parse(in, handler);
			return handler.getInfos();
		} catch (final Exception e) {
			throw new SubversionException("could not parse input", e);
		}
	}

	private ResourceProperty[] customProperties = EMPTY;

	private boolean directory;

	// NOTE: not part of xml response but determined by a response header
	private String lockOwner;

	private String lockToken;

	private String md5;

	private String repositoryUuid;

	private Resource resource;

	private Revision revision;

	InfoEntry() {
		// prevent direct instantiation
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final InfoEntry other = (InfoEntry) obj;
		if (resource == null) {
			if (other.resource != null) {
				return false;
			}
		} else if (!resource.equals(other.resource)) {
			return false;
		}
		if (repositoryUuid == null) {
			if (other.repositoryUuid != null) {
				return false;
			}
		} else if (!repositoryUuid.equals(other.repositoryUuid)) {
			return false;
		}
		if (revision == null) {
			if (other.revision != null) {
				return false;
			}
		} else if (!revision.equals(other.revision)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns an array of the custom {@link ResourceProperty}
	 * @return the array of the custom {@link ResourceProperty} or an empty array if there a non
	 */
	public ResourceProperty[] getCustomProperties() {
		return Arrays.copyOf(customProperties, customProperties.length);
	}

	/**
	 * Returns a name of the lock owner
	 * @return the name of the lock owner or {@code null} if the resource is not locked
	 */
	@CheckForNull
	public String getLockOwner() {
		return lockOwner;
	}

	/**
	 * Returns a lock-token
	 * @return the lock-token or {@code null} if the resource is not locked
	 */
	@CheckForNull
	public String getLockToken() {
		return lockToken;
	}

	/**
	 * Returns a MD5 checksum of the resource
	 * @return the MD5 checksum of the resource or {@code null} if the resource is a directory
	 */
	@CheckForNull
	public String getMd5() {
		return md5;
	}

	/**
	 * Returns a globally unique identifier of the repository
	 * @return the globally unique identifier of the repository
	 */
	public String getRepositoryUuid() {
		return repositoryUuid;
	}

	/**
	 * Returns a {@link Resource} of the resource (relative to the root of the repository)
	 * @return the {@link Resource} of the resource (relative to the root of the repository)
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Returns the value of the custom property with the given name
	 * @param name name of the custom property
	 * @return the value of the custom property or {@code null} if no custom property with the given name was found
	 */
	@CheckForNull
	public String getResourcePropertyValue(final String name) {
		for (final ResourceProperty property : customProperties) {
			if (name.equals(property.getName())) {
				return property.getValue();
			}
		}
		return null;
	}

	/**
	 * Returns a {@link Revision} of the resource
	 * @return the {@link Revision} of the resource
	 */
	public Revision getRevision() {
		return revision;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((resource == null) ? 0 : resource.hashCode());
		result = (prime * result) + ((repositoryUuid == null) ? 0 : repositoryUuid.hashCode());
		result = (prime * result) + ((revision == null) ? 0 : revision.hashCode());
		return result;
	}

	/**
	 * Determines if the resource is a directory
	 * @return {@code true} if the resource is a directory otherwise {@code false}
	 */
	public boolean isDirectory() {
		return directory;
	}

	/**
	 * Determines if the resource is a file
	 * @return {@code true} if the resource is a file otherwise {@code false}
	 */
	public boolean isFile() {
		return !directory;
	}

	/**
	 * Determines if the resource is locked
	 * @return {@code true} if the resource is locked otherwise {@code false}
	 */
	public boolean isLocked() {
		return lockToken != null;
	}

	void setCustomProperties(@Nullable final ResourceProperty[] customProperties) {
		if ((customProperties == null) || (customProperties.length == 0)) {
			this.customProperties = EMPTY;
		} else {
			this.customProperties = Arrays.copyOf(customProperties, customProperties.length);
		}
	}

	void setDirectory(final boolean directory) {
		this.directory = directory;
	}

	void setFile(final boolean file) {
		directory = !file;
	}

	void setLockOwner(final String lockOwner) {
		this.lockOwner = lockOwner;
	}

	void setLockToken(final String lockToken) {
		this.lockToken = lockToken;
	}

	void setMd5(final String md5) {
		this.md5 = md5;
	}

	void setRepositoryUuid(final String repositoryUuid) {
		this.repositoryUuid = repositoryUuid;
	}

	void setResource(final Resource resource) {
		this.resource = resource;
	}

	void setRevision(final Revision revision) {
		this.revision = revision;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("SubversionInfo [customProperties=");
		builder.append(Arrays.toString(customProperties));
		builder.append(", directory=");
		builder.append(directory);
		builder.append(", lockOwner=");
		builder.append(lockOwner);
		builder.append(", lockToken=");
		builder.append(lockToken);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", resource=");
		builder.append(resource);
		builder.append(", repositoryUuid=");
		builder.append(repositoryUuid);
		builder.append(", revision=");
		builder.append(revision);
		builder.append("]");
		return builder.toString();
	}
}
