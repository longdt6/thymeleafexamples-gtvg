package thymeleafexamples.gtvg.business.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;
import thymeleafexamples.gtvg.business.persistence.DatabaseDemoSpringContext;

class DatabaseDemoServiceTest {

    @AfterEach
    void closeSpringContext() {
        DatabaseDemoSpringContext.closeForTests();
    }

    @Test
    void returnsNotConfiguredWhenDatabaseUrlIsMissing() {
        final DatabaseDemoService service = new DatabaseDemoService(() -> null);

        final DatabaseDemoStatus status = service.getStatus();

        assertFalse(status.isConfigured());
        assertFalse(status.isConnected());
        assertEquals("DATABASE_URL is not configured yet.", status.getMessage());
    }

    @Test
    void persistsHeartbeatThroughSpringDataJpaAndHibernate() {
        final String databaseUrl =
                "jdbc:h2:mem:dbdemo" + System.nanoTime() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        final DatabaseDemoService service = new DatabaseDemoService(() -> databaseUrl);

        final DatabaseDemoStatus firstStatus = service.getStatus();
        final DatabaseDemoStatus secondStatus = service.getStatus();

        assertTrue(firstStatus.isConfigured());
        assertTrue(firstStatus.isConnected());
        assertEquals(Long.valueOf(1L), firstStatus.getHeartbeatCount());
        assertTrue(secondStatus.isConnected());
        assertEquals(Long.valueOf(2L), secondStatus.getHeartbeatCount());
        assertEquals("Connected through Spring Data JPA and Hibernate.", secondStatus.getMessage());
        assertNotNull(secondStatus.getDatabaseVersion());
    }

}
