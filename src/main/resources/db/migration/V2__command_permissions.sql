create sequence command_permission_seq
    start with 1
    increment by 50;

create table command_permission (
    id           bigint not null default nextval('command_permission_seq'),
    guild_id     bigint not null,
    command_name text   not null,
    min_role_id  bigint,
    constraint pk_command_permission primary key (id),
    constraint uq_command_permission_guild_command unique (guild_id, command_name)
);

alter sequence command_permission_seq owned by command_permission.id;

create sequence command_permission_explicit_role_seq
    start with 1
    increment by 50;

create table command_permission_explicit_role (
    id                    bigint not null default nextval('command_permission_explicit_role_seq'),
    command_permission_id bigint not null,
    role_id               bigint not null,
    constraint pk_command_permission_explicit_role primary key (id),
    constraint uq_command_permission_explicit_role unique (command_permission_id, role_id),
    constraint fk_command_permission_explicit_role
        foreign key (command_permission_id)
        references command_permission(id)
        on delete cascade
);

alter sequence command_permission_explicit_role_seq owned by command_permission_explicit_role.id;