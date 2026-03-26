ALTER TABLE penalty
    ADD CONSTRAINT fk_penalty_penalty_type
    FOREIGN KEY (penalty_type_pkid) REFERENCES penalty_type(id);
