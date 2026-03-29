package com.esemudeo.quarkus.penaltybot.configuration.commandpermission.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.annotation.Nonnull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
		name = CommandPermission.TABLE_NAME,
		uniqueConstraints = @UniqueConstraint(
				name = "uq_command_permission_guild_command",
				columnNames = {CommandPermission.COLUMN_GUILD_ID, CommandPermission.COLUMN_COMMAND_NAME}
		)
)
public class CommandPermission extends PanacheEntity {

	static final String COLUMN_GUILD_ID = "guild_id";
	static final String COLUMN_COMMAND_NAME = "command_name";
	static final String TABLE_NAME = "command_permission";

	@Column(name = COLUMN_GUILD_ID, nullable = false)
	private @Nonnull Long guildId;

	@Column(name = COLUMN_COMMAND_NAME, nullable = false)
	private @Nonnull String commandName;

	@Column(name = "min_role_id")
	private Long minRoleId;

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = CommandExplicitRole.COMMAND_PERMISSION_ID, nullable = false)
	private List<CommandExplicitRole> explicitRoles = new ArrayList<>();
}