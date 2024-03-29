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

import java.net.URI;

import de.methodpark.subversion.Repository;
import de.methodpark.subversion.Resource;
import de.methodpark.subversion.Revision;
import de.methodpark.subversion.SubversionException;
import de.methodpark.subversion.View;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryDownloadUriIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/download");

    private final Repository repository;

    protected AbstractRepositoryDownloadUriIT(final Repository repository) {
        this.repository = repository;
    }

    private String createMessage(final Resource resource, final Revision revision) {
        return resource + ": @" + revision;
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        repository.downloadURI(resource, revision);
        Assert.fail("downloadURI must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.downloadURI(resource, revision);
        Assert.fail("downloadURI must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final View view = repository.createView();
        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, view.getHeadRevision()));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision revision = Revision.create(22);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(25);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(27);

        final AbstractBaseRepository ar = (AbstractBaseRepository) repository;
        final QualifiedResource qualifiedResource = new QualifiedResource(repository.getBasePath(), resource);
        final URI expected = URIUtils.appendResources(repository.getBaseUri(), ar.config.getVersionedResource(qualifiedResource, revision));
        final String message = createMessage(resource, revision);
        Assert.assertEquals(message, expected, repository.downloadURI(resource, revision));
    }
}
