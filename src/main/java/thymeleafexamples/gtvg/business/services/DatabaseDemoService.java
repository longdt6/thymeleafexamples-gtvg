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
package thymeleafexamples.gtvg.business.services;

import java.sql.SQLException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;
import thymeleafexamples.gtvg.business.persistence.DatabaseDemoGateway;
import thymeleafexamples.gtvg.business.persistence.DatabaseDemoPersistenceConfig;

@Service
@RequiredArgsConstructor
public class DatabaseDemoService {

    private final Environment environment;
    private final ObjectProvider<DatabaseDemoGateway> databaseDemoGateway;

    public DatabaseDemoStatus getStatus() {
        final String databaseUrl =
                this.environment.getProperty(DatabaseDemoPersistenceConfig.DATABASE_URL_PROPERTY);
        if (!StringUtils.hasText(databaseUrl)) {
            return DatabaseDemoStatus.builder()
                    .configured(false)
                    .connected(false)
                    .message("DATABASE_URL is not configured yet.")
                    .build();
        }

        try {
            return this.databaseDemoGateway.getObject().recordHeartbeatAndReadStatus();
        } catch (final SQLException e) {
            return failedStatus("Could not connect to PostgreSQL: " + e.getMessage());
        } catch (final RuntimeException e) {
            return failedStatus("DATABASE_URL is invalid: " + e.getMessage());
        }
    }

    private static DatabaseDemoStatus failedStatus(final String message) {
        return DatabaseDemoStatus.builder()
                .configured(true)
                .connected(false)
                .message(message)
                .build();
    }

}
