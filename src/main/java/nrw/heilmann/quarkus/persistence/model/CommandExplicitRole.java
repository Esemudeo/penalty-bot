package nrw.heilmann.quarkus.persistence.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
		name = CommandExplicitRole.TABLE_NAME,
		uniqueConstraints = @UniqueConstraint(
				name = "uq_" + CommandExplicitRole.TABLE_NAME,
				columnNames = {CommandExplicitRole.COMMAND_ID, CommandExplicitRole.COLUMN_ROLE_ID}
		)
)
public class CommandExplicitRole extends PanacheEntity {

	static final String COMMAND_ID = "command_permission_id";
	static final String COLUMN_ROLE_ID = "role_id";
	static final String TABLE_NAME = "command_explicit_role";

	@Column(name = COMMAND_ID, nullable = false)
	private @Nonnull Long commandId;

	@Column(name = COLUMN_ROLE_ID, nullable = false)
	private @Nonnull Long roleId;
}
