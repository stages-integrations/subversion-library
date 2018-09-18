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
package de.shadowhunt.subversion.cmdl;

import java.io.PrintStream;
import java.net.URI;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import de.shadowhunt.subversion.Info;
import de.shadowhunt.subversion.LockToken;
import de.shadowhunt.subversion.Repository;
import de.shadowhunt.subversion.RepositoryFactory;
import de.shadowhunt.subversion.Resource;
import de.shadowhunt.subversion.ResourceProperty;
import de.shadowhunt.subversion.Revision;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public class InfoCommand extends AbstractCommand {

    public InfoCommand() {
        super("info");
    }

    @Override
    public boolean call(final PrintStream output, final PrintStream error, final String... args) throws Exception {
        final OptionParser parser = createParser();
        final OptionSpec<URI> baseOption = createBaseOption(parser);
        final OptionSpec<Resource> resourceOption = createResourceOption(parser);
        final OptionSpec<String> usernameOption = createUsernameOption(parser);
        final OptionSpec<String> passwordOption = createPasswordOption(parser);
        final OptionSpec<Void> sslOption = createSslOption(parser);
        final OptionSpec<Revision> revisionOption = createRevisionOption(parser);

        final OptionSet options = parse(output, error, parser, args);
        if (options == null) {
            return false;
        }

        final String username = usernameOption.value(options);
        final String password = passwordOption.value(options);
        final boolean allowAllSsl = options.has(sslOption);
        try (CloseableHttpClient client = createHttpClient(username, password, allowAllSsl)) {
            final RepositoryFactory factory = RepositoryFactory.getInstance();

            final HttpContext context = createHttpContext();
            final URI base = baseOption.value(options);
            final Repository repository = factory.createRepository(base, client, context, true);

            final Resource resource = resourceOption.value(options);
            final Revision revision = revisionOption.value(options);
            final Info info = repository.info(resource, revision);

            final Resource infoResource = info.getResource();
            output.println("Reource: " + infoResource);
            final URI baseUri = repository.getBaseUri();
            output.println("URL: " + baseUri + infoResource);
            output.println("Repository Root: " + baseUri);
            final UUID repositoryId = info.getRepositoryId();
            output.println("Repository UUID: " + repositoryId);
            output.println("Revision: " + revision);
            if (info.isDirectory()) {
                output.println("Node Kind: directory");
            } else {
                output.println("Node Kind: file");
            }
            final Date creationDate = info.getCreationDate();
            output.println("Creation Date: " + creationDate);
            final Revision lastRevison = info.getRevision();
            output.println("Last Changed Rev: " + lastRevison);
            final Date lastModifiedDate = info.getLastModifiedDate();
            output.println("Last Changed Date: " + lastModifiedDate);
            final Optional<LockToken> lockToken = info.getLockToken();
            if (lockToken.isPresent()) {
                final LockToken lockTokenValue = lockToken.get();
                output.println("Lock Token: opaquelocktoken: " + lockTokenValue);
            }
            final Optional<String> lockOwner = info.getLockOwner();
            if (lockOwner.isPresent()) {
                final String lockOwnerValue = lockOwner.get();
                output.println("Lock Owner: " + lockOwnerValue);
            }
            final Optional<String> md5 = info.getMd5();
            if (md5.isPresent()) {
                final String md5Value = md5.get();
                output.println("Checksum: " + md5Value);
            }
            output.println();

            output.println("Properties:");
            final ResourceProperty[] properties = info.getProperties();
            for (final ResourceProperty property : properties) {
                final String name = property.getName();
                final String value = property.getValue();
                output.println("  " + name + " = " + value);
            }
        }
        return true;
    }

}
