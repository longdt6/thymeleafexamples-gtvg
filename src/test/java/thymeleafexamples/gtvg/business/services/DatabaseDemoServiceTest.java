package thymeleafexamples.gtvg.business.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;
import thymeleafexamples.gtvg.business.persistence.DatabaseDemoGateway;

class DatabaseDemoServiceTest {

    @Test
    void returnsNotConfiguredWhenDatabaseUrlIsMissing() {
        final ObjectProvider<DatabaseDemoGateway> databaseDemoGateway = new ObjectProvider<DatabaseDemoGateway>() {
        };
        final DatabaseDemoService service =
                new DatabaseDemoService(new MockEnvironment(), databaseDemoGateway);

        final DatabaseDemoStatus status = service.getStatus();

        assertFalse(status.isConfigured());
        assertFalse(status.isConnected());
        assertEquals("DATABASE_URL is not configured yet.", status.getMessage());
    }

}
