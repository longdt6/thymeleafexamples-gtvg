package thymeleafexamples.gtvg.business.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.env.MockEnvironment;
import thymeleafexamples.gtvg.business.entities.DatabaseDemoStatus;
import thymeleafexamples.gtvg.business.persistence.DatabaseDemoGateway;

class DatabaseDemoServiceTest {

    @Test
    void returnsNotConfiguredWhenDatabaseUrlIsMissing() {
        final DatabaseDemoService service =
                new DatabaseDemoService(new MockEnvironment(), mock(ObjectProvider.class));

        final DatabaseDemoStatus status = service.getStatus();

        assertFalse(status.isConfigured());
        assertFalse(status.isConnected());
        assertEquals("DATABASE_URL is not configured yet.", status.getMessage());
    }

}
