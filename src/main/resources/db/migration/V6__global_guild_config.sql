CREATE SEQUENCE global_guild_config_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE global_guild_config
(
    id                 BIGINT NOT NULL DEFAULT nextval('global_guild_config_seq'),
    guild_id           BIGINT NOT NULL,
    paypal_me_username VARCHAR(255),
    CONSTRAINT pk_global_guild_config PRIMARY KEY (id),
    CONSTRAINT uq_global_guild_config_guild UNIQUE (guild_id)
);
