create sequence penalty_seq
    start with 1
    increment by 50;

create table penalty (
    id bigint not null default nextval('penalty_seq'),
    "timestamp" timestamp with time zone not null,
    guild_id bigint not null,
    author_id bigint not null,
    affected_member_id bigint not null,
    amount integer not null,
    penalty_type_pkid bigint not null,
    constraint pk_penalty primary key (id)
);

alter sequence penalty_seq owned by penalty.id;

create sequence penalty_type_seq
    start with 1
    increment by 50;

create table penalty_type (
    id bigint not null default nextval('penalty_type_seq'),
    guild_id bigint not null,
    technical_name text not null,
    display_name text not null,
    constraint pk_penalty_type primary key (id)
);

alter sequence penalty_type_seq owned by penalty_type.id;
