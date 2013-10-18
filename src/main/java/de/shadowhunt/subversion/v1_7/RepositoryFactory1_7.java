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
package de.shadowhunt.subversion.v1_7;

import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Version;
import java.net.URI;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link RepositoryFactory1_7} can create {@link Repository} that support subversion servers of version 1.7.X
 */
@ThreadSafe
public class RepositoryFactory1_7 implements RepositoryFactory {

	@Override
	public Repository createRepository(final URI repository, final boolean trustServerCertificat) {
		return new Repository1_7(repository, trustServerCertificat);
	}

	@Override
	public boolean isServerVersionSupported(final Version version) {
		return Version.HTTPv2 == version;
	}

}
