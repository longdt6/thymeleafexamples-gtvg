/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package thymeleafexamples.gtvg.business.persistence;

import java.util.Collections;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

public final class DatabaseDemoSpringContext {

    private static volatile ContextHolder contextHolder = null;

    public static DatabaseDemoGateway getGateway(final String databaseUrl) {
        return getContext(databaseUrl).getBean(DatabaseDemoGateway.class);
    }

    public static synchronized void closeForTests() {
        if (contextHolder != null) {
            contextHolder.close();
            contextHolder = null;
        }
    }

    private static AnnotationConfigApplicationContext getContext(final String databaseUrl) {
        final ContextHolder current = contextHolder;
        if (current != null && current.uses(databaseUrl)) {
            return current.getContext();
        }
        synchronized (DatabaseDemoSpringContext.class) {
            final ContextHolder synchronizedCurrent = contextHolder;
            if (synchronizedCurrent != null && synchronizedCurrent.uses(databaseUrl)) {
                return synchronizedCurrent.getContext();
            }
            if (synchronizedCurrent != null) {
                synchronizedCurrent.close();
            }
            contextHolder = ContextHolder.create(databaseUrl);
            return contextHolder.getContext();
        }
    }

    private DatabaseDemoSpringContext() {
        super();
    }

    private static final class ContextHolder {

        private final String databaseUrl;
        private final AnnotationConfigApplicationContext context;

        private ContextHolder(final String databaseUrl, final AnnotationConfigApplicationContext context) {
            this.databaseUrl = databaseUrl;
            this.context = context;
        }

        private static ContextHolder create(final String databaseUrl) {
            final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource(
                            "databaseDemo",
                            Collections.singletonMap(DatabaseDemoPersistenceConfig.DATABASE_URL_PROPERTY, databaseUrl)));
            context.register(DatabaseDemoPersistenceConfig.class);
            context.refresh();
            return new ContextHolder(databaseUrl, context);
        }

        private boolean uses(final String databaseUrl) {
            return this.databaseUrl.equals(databaseUrl);
        }

        private AnnotationConfigApplicationContext getContext() {
            return this.context;
        }

        private void close() {
            this.context.close();
        }
    }

}
