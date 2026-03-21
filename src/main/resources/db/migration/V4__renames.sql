-- rename tables
ALTER TABLE command_permission RENAME TO command;
ALTER TABLE command_permission_explicit_role RENAME TO command_explicit_role;

-- rename sequences
ALTER SEQUENCE command_permission_seq RENAME TO command_seq;
ALTER SEQUENCE command_permission_explicit_role_seq RENAME TO command_explicit_role_seq;

-- rename constraints
ALTER TABLE command
    RENAME CONSTRAINT pk_command_permission TO pk_command;
ALTER TABLE command_explicit_role
    RENAME COLUMN command_permission_id TO command_id;
ALTER TABLE command
    RENAME CONSTRAINT uq_command_permission_guild_command TO uq_command_guild_command;
ALTER TABLE command_explicit_role
    RENAME CONSTRAINT pk_command_permission_explicit_role TO pk_command_explicit_role;
ALTER TABLE command_explicit_role
    RENAME CONSTRAINT uq_command_permission_explicit_role TO uq_command_explicit_role;
ALTER TABLE command_explicit_role
    RENAME CONSTRAINT fk_command_permission_explicit_role TO fk_command_explicit_role;
