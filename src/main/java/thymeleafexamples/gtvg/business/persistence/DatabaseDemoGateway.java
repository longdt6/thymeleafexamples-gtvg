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

import java.sql.SQLException;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;

@Service
@RequiredArgsConstructor
public class DatabaseDemoGateway {

    private static final String HEARTBEAT_MESSAGE = "SMEConnect Render Postgres demo";

    private final RenderDemoHeartbeatRepository heartbeatRepository;
    private final DatabaseConnectionProperties connectionProperties;
    private final DatabaseMetadataReader metadataReader;

    @Transactional
    public DatabaseDemoStatus recordHeartbeatAndReadStatus() throws SQLException {
        this.heartbeatRepository.saveAndFlush(new RenderDemoHeartbeat(HEARTBEAT_MESSAGE));
        final long heartbeatCount = this.heartbeatRepository.count();
        final DatabaseMetadata metadata = this.metadataReader.read();

        return DatabaseDemoStatus.builder()
                .configured(true)
                .connected(true)
                .message("Connected through Spring Data JPA and Hibernate.")
                .dataSource(this.connectionProperties.getSafeDataSource())
                .databaseName(metadata.getDatabaseName())
                .databaseUser(metadata.getDatabaseUser())
                .databaseVersion(metadata.getDatabaseVersion())
                .databaseTime(Instant.now().toString())
                .heartbeatCount(Long.valueOf(heartbeatCount))
                .build();
    }

}
