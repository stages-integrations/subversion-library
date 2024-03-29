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

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import de.methodpark.subversion.Repository;
import de.methodpark.subversion.Resource;
import de.methodpark.subversion.Revision;
import de.methodpark.subversion.SubversionException;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

// Tests are independent from each other but go from simple to more complex
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractRepositoryDownloadIT {

    public static final Resource PREFIX = Resource.create("/00000000-0000-0000-0000-000000000000/download");

    public static void assertEquals(final String message, final InputStream expected, final InputStream actual) throws Exception {
        final String expectedString = IOUtils.toString(expected, StandardCharsets.UTF_8);
        final String actualString = IOUtils.toString(actual, StandardCharsets.UTF_8);
        Assert.assertEquals(message, expectedString.trim(), actualString.trim());
    }

    private final DownloadLoader downloadLoader;

    private final Repository repository;

    protected AbstractRepositoryDownloadIT(final Repository repository, final File root) {
        this.repository = repository;
        downloadLoader = new DownloadLoader(root, repository.getBasePath());
    }

    private String createMessage(final Resource resource, final Revision revision) {
        return resource + ": @" + revision;
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingResource() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/non_existing.txt"));
        final Revision revision = Revision.HEAD;

        repository.download(resource, revision);
        Assert.fail("download must not complete");
    }

    @Test(expected = SubversionException.class)
    public void test00_NonExistingRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        // there should not be a such high revision
        final Revision revision = Revision.create(Integer.MAX_VALUE);

        repository.download(resource, revision);
        Assert.fail("download must not complete");
    }

    @Test
    public void test01_FileHead() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file.txt"));
        final Revision revision = Revision.HEAD;

        final String message = createMessage(resource, revision);
        try (InputStream expected = downloadLoader.load(resource, revision)) {
            try (InputStream actual = repository.download(resource, revision)) {
                assertEquals(message, expected, actual);
            }
        }
    }

    @Test
    public void test01_FileRevision() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_delete.txt"));
        final Revision revision = Revision.create(22);

        final InputStream expected = downloadLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertEquals(message, expected, repository.download(resource, revision));
    }

    @Test
    public void test02_FileCopy() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_copy.txt"));
        final Revision revision = Revision.create(25);

        final InputStream expected = downloadLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertEquals(message, expected, repository.download(resource, revision));
    }

    @Test
    public void test02_FileMove() throws Exception {
        final Resource resource = PREFIX.append(Resource.create("/file_move.txt"));
        final Revision revision = Revision.create(27);

        final InputStream expected = downloadLoader.load(resource, revision);
        final String message = createMessage(resource, revision);
        assertEquals(message, expected, repository.download(resource, revision));
    }
}
