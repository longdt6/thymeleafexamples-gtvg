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

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

@Configuration
@ComponentScan(basePackageClasses = DatabaseDemoGateway.class)
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = RenderDemoHeartbeatRepository.class)
public class DatabaseDemoPersistenceConfig {

    static final String DATABASE_URL_PROPERTY = "database.url";

    @Bean
    public DatabaseConnectionProperties databaseConnectionProperties(final Environment environment) {
        return DatabaseConnectionProperties.from(environment.getRequiredProperty(DATABASE_URL_PROPERTY));
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(final DatabaseConnectionProperties properties) {
        final HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("render-demo-jpa");
        dataSource.setJdbcUrl(properties.getJdbcUrl());
        dataSource.setMaximumPoolSize(3);
        dataSource.setMinimumIdle(0);
        if (StringUtils.hasText(properties.getUsername())) {
            dataSource.setUsername(properties.getUsername());
        }
        if (StringUtils.hasText(properties.getPassword())) {
            dataSource.setPassword(properties.getPassword());
        }
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setShowSql(false);

        final Map<String, Object> jpaProperties = new HashMap<String, Object>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "update");
        jpaProperties.put("hibernate.jdbc.time_zone", "UTC");

        final LocalContainerEntityManagerFactoryBean factory =
                new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(RenderDemoHeartbeat.class.getPackage().getName());
        factory.setJpaPropertyMap(jpaProperties);
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public DatabaseMetadataReader databaseMetadataReader(final DataSource dataSource) {
        return new DatabaseMetadataReader(dataSource);
    }

}
