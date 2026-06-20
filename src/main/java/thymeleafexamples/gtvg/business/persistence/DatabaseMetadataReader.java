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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DatabaseMetadataReader {

    private final DataSource dataSource;

    public DatabaseMetadata read() throws SQLException {
        try (Connection connection = this.dataSource.getConnection()) {
            final DatabaseMetaData metaData = connection.getMetaData();
            final String databaseVersion =
                    metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion();
            return new DatabaseMetadata(connection.getCatalog(), metaData.getUserName(), databaseVersion);
        }
    }

}
