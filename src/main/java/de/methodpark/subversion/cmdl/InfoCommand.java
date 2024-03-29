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
package de.methodpark.subversion.cmdl;

import java.io.PrintStream;
import java.net.URI;
import java.util.Optional;

import de.methodpark.subversion.Info;
import de.methodpark.subversion.LockToken;
import de.methodpark.subversion.Repository;
import de.methodpark.subversion.AbstractRepositoryFactory;
import de.methodpark.subversion.Resource;
import de.methodpark.subversion.ResourceProperty;
import de.methodpark.subversion.Revision;
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
            final AbstractRepositoryFactory factory = AbstractRepositoryFactory.getInstance();

            final HttpContext context = createHttpContext();
            final URI base = baseOption.value(options);
            final Repository repository = factory.createRepository(base, client, context, true);

            final Resource resource = resourceOption.value(options);
            final Revision revision = revisionOption.value(options);
            final Info info = repository.info(resource, revision);
            output.println("Reource: " + info.getResource());
            output.println("URL: " + repository.getBaseUri() + info.getResource());
            output.println("Repository Root: " + repository.getBaseUri());
            output.println("Repository UUID: " + info.getRepositoryId());
            output.println("Revision: " + revision);
            if (info.isDirectory()) {
                output.println("Node Kind: directory");
            } else {
                output.println("Node Kind: file");
            }
            output.println("Creation Date: " + info.getCreationDate());
            output.println("Last Changed Rev: " + info.getRevision());
            output.println("Last Changed Date: " + info.getLastModifiedDate());
            final Optional<LockToken> lockToken = info.getLockToken();
            if (lockToken.isPresent()) {
                output.println("Lock Token: opaquelocktoken: " + lockToken.get());
            }
            final Optional<String> lockOwner = info.getLockOwner();
            if (lockOwner.isPresent()) {
                output.println("Lock Owner: " + lockOwner.get());
            }
            final Optional<String> md5 = info.getMd5();
            if (md5.isPresent()) {
                output.println("Checksum: " + md5.get());
            }
            output.println();

            output.println("Properties:");
            final ResourceProperty[] properties = info.getProperties();
            for (final ResourceProperty property : properties) {
                output.println("  " + property.getName() + " = " + property.getValue());
            }
        }
        return true;
    }

}
