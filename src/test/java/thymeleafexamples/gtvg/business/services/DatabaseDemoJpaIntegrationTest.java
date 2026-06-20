package thymeleafexamples.gtvg.business.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import thymeleafexamples.gtvg.GtvgApplication;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;

@SpringBootTest(
        classes = GtvgApplication.class,
        properties = {
                "database.url=jdbc:h2:mem:dbdemo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1"
        })
class DatabaseDemoJpaIntegrationTest {

    @Autowired
    private DatabaseDemoService databaseDemoService;

    @Test
    void persistsHeartbeatThroughSpringDataJpaAndHibernate() {
        final DatabaseDemoStatus firstStatus = this.databaseDemoService.getStatus();
        final DatabaseDemoStatus secondStatus = this.databaseDemoService.getStatus();

        assertTrue(firstStatus.isConfigured());
        assertTrue(firstStatus.isConnected());
        assertEquals(Long.valueOf(1L), firstStatus.getHeartbeatCount());
        assertTrue(secondStatus.isConnected());
        assertEquals(Long.valueOf(2L), secondStatus.getHeartbeatCount());
        assertEquals("Connected through Spring Data JPA and Hibernate.", secondStatus.getMessage());
        assertNotNull(secondStatus.getDatabaseVersion());
    }

}
