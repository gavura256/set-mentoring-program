package com.bookshop;

import com.bookshop.config.TestContainersConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all controller-level integration tests.
 * Starts a real MySQL container via Testcontainers and wires the datasource
 * automatically through @ServiceConnection — no property overrides needed.
 *
 * WARNING: Adding @MockBean or @SpyBean in a subclass creates a separate
 * ApplicationContext, breaking context sharing and potentially causing
 * unexpected isolation from the shared container transaction.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
@Import(TestContainersConfig.class)
public abstract class AbstractIntegrationTest {
}
