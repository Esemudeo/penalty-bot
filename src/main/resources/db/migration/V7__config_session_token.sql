create sequence config_session_token_seq
    start with 1
    increment by 50;

create table config_session_token (
    id bigint not null default nextval('config_session_token_seq'),
    guild_id bigint not null,
    user_id bigint not null,
    token varchar(64) not null,
    expires_at timestamp with time zone not null,
    used boolean not null default false,
    constraint pk_config_session_token primary key (id),
    constraint uq_config_session_token_token unique (token)
);

alter sequence config_session_token_seq owned by config_session_token.id;