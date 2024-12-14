-- DROP tables in reverse order first.
DROP TABLE IF EXISTS builds;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS attachment_parts;
DROP TABLE IF EXISTS movement_parts;
DROP TABLE IF EXISTS energy_source_parts;
DROP TABLE IF EXISTS body_parts;
DROP TABLE IF EXISTS energy_source_size;
DROP TABLE IF EXISTS arm_parts;
DROP TABLE IF EXISTS head_parts;
DROP TABLE IF EXISTS parts;
DROP TABLE IF EXISTS manufacturers;
DROP TABLE IF EXISTS part_types;
DROP TABLE IF EXISTS sub_types;
DROP TABLE IF EXISTS super_types;

-- CREATE tables.

-- Perhaps a solution from one of the answers in the below link would be better
-- for the three tables below?
-- See Also: https://stackoverflow.com/questions/12032348/how-are-super-and-subtype-relationships-in-er-diagrams-represented-as-tables/12037398#12037398
CREATE TABLE super_types (
    name VARCHAR(16) PRIMARY KEY
);

CREATE TABLE sub_types (
    name VARCHAR(16) PRIMARY KEY
);

CREATE TABLE part_types (
    super_type_name VARCHAR(16) REFERENCES super_types(name),
    sub_type_name VARCHAR(16) REFERENCES sub_types(name),
    PRIMARY KEY (super_type_name, sub_type_name)
);

CREATE TABLE manufacturers (
    id INTEGER PRIMARY KEY,
    name VARCHAR(64)
);

-- NOTE: There are multiple repeated TODOs in the below tables. It might be
--       best to move the table fields around or rework the design for all the
--       parts tables to reduce the amount CHECK constraints.
--       Also consider giving part_types above an INTEGER PRIMARY KEY to not
--       have to repeat both super and sub keys so many times.
CREATE TABLE parts (
    id INTEGER PRIMARY KEY,
    manufacturer_id INTEGER REFERENCES manufacturers(id),
    model CHAR(12) NOT NULL,
    description TEXT,
    part_type_super VARCHAR(16) NOT NULL REFERENCES part_types(super_type_name),
    part_type_sub VARCHAR(16) NOT NULL REFERENCES part_types(sub_type_name)
);

CREATE TABLE head_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    -- TODO: CHECK for >= 0, above 100 is okay! That means newer better stuff!
    durability_score SMALLINT NOT NULL,
    -- TODO: CHECK for >= 0.
    weight INTEGER NOT NULL
);

CREATE TABLE arm_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    durability_score SMALLINT NOT NULL, -- TODO: See head_parts(durability_score).
    weight INTEGER NOT NULL -- TODO: See head_parts(weight).
);

CREATE TABLE energy_source_size (
    name CHAR(3) PRIMARY KEY -- A10, A20, B25, etc.
);

CREATE TABLE body_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    durability_score SMALLINT NOT NULL, -- TODO: See head_parts(durability_score).
    weight INTEGER NOT NULL, -- TODO: See head_parts(weight).
    energy_source_size CHAR(3) NOT NULL REFERENCES energy_source_size(name)
);

CREATE TABLE energy_source_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    weight INTEGER NOT NULL, -- TODO: See head_parts(weight).
    -- TODO: CHECK for >= 0.
    energy_source_size_name CHAR(3) NOT NULL REFERENCES energy_source_size(name),
    energy_capacity INTEGER NOT NULL
);

CREATE TABLE movement_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    durability_score SMALLINT NOT NULL, -- TODO: See head_parts(durability_score).
    weight INTEGER NOT NULL, -- TODO: See head_parts(weight).
    -- TODO: CHECK for >= 0.
    max_speed SMALLINT NOT NULL,
    -- TODO: CHECK for >= 0.
    energy_draw SMALLINT NOT NULL
);

CREATE TABLE attachment_parts (
    id INTEGER PRIMARY KEY,
    part_id INTEGER NOT NULL REFERENCES parts(id),
    weight INTEGER NOT NULL, -- TODO: See head_parts(weight).
    -- TODO: CHECK for >= 0.
    energy_draw SMALLINT NOT NULL
);

CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    user_name VARCHAR(64) NOT NULL,
    password CHAR(103) NOT NULL
);

CREATE TABLE builds (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(id),
    head_part_id INTEGER NOT NULL REFERENCES head_parts(id),
    left_arm_part_id INTEGER NOT NULL REFERENCES arm_parts(id),
    right_arm_part_id INTEGER NOT NULL REFERENCES arm_parts(id),
    body_part_id INTEGER NOT NULL REFERENCES body_parts(id),
    energy_source_part_id INTEGER NOT NULL REFERENCES energy_source_parts(id),
    movement_part_id INTEGER NOT NULL REFERENCES movement_parts(id),
    a_attachment_part_id INTEGER REFERENCES attachment_parts(id),
    b_attachment_part_id INTEGER REFERENCES attachment_parts(id),
    c_attachment_part_id INTEGER REFERENCES attachment_parts(id),
    d_attachment_part_id INTEGER REFERENCES attachment_parts(id)
);
