package nrw.heilmann.quarkus.persistence;

import io.agroal.api.AgroalDataSource;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import nrw.heilmann.quarkus.persistence.model.Penalty;
import nrw.heilmann.quarkus.persistence.model.PenaltyType;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
class DevServicesPersistenceTest {

	@Inject
	AgroalDataSource dataSource;

	@Test
	void datasource_is_postgresql_in_test_profile() throws Exception {
		try (Connection c = dataSource.getConnection()) {
			assertEquals("PostgreSQL", c.getMetaData().getDatabaseProductName());
			assertNotNull(c.getMetaData().getURL());
		}
	}

	@Test
	@TestTransaction
	void persist_works_without_explicit_test_db_details() {
		long before = Penalty.count();

		Penalty tk = Penalty.builder()
				.timestamp(Instant.now())
				.guildId(3L)
				.authorId(1L)
				.affectedMemberId(2L)
				.amount(1)
				.penaltyType(PenaltyType.builder().guildId(3L).technicalName("teamkill").displayName("Teamkill").build())
				.build();

		assertNull(tk.id, "ID sollte vor dem Persistieren null sein");

		tk.persist();

		assertNotNull(tk.id, "Persist sollte eine ID vergeben");
		assertEquals(before + 1, Penalty.count(), "Datensatz sollte innerhalb der Test-Transaktion sichtbar sein");
	}
}
