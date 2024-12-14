-- Seed the database tables with usefull values.
INSERT INTO super_types(name) VALUES
("Head"),
("Arm"),
("Body"),
("Energy Source"),
("Movement"),
("Attachment");

INSERT INTO sub_types(name) VALUES
-- Default sub type.
("Standard"),
-- Movement sub types.
("Wheels"),
("Tracks"),
("Legs"),
("Hover Devices"),
-- Attachment sub types.
("Offensive"),
("Defensive"),
("Utility"),
("Miscellaneous");

INSERT INTO part_types(super_type_name, sub_type_name) VALUES
-- Super types without cool subtypes (yet!)
("Head", "Standard"),
("Arm", "Standard"),
("Body", "Standard"),
("Energy Source", "Standard"),
-- Valid movement types.
("Movement", "Wheels"),
("Movement", "Tracks"),
("Movement", "Legs"),
("Movement", "Hover Devices"),
-- Valid attachment types.
("Attachment", "Offensive"),
("Attachment", "Defensive"),
("Attachment", "Utility"),
("Attachment", "Miscellaneous");

INSERT INTO manufacturers(name) VALUES
-- TODO: New _original_ names...
("Triakis"),
("Harimau"),
("Sylva"),
("Holobot"),
("Lunar Parcs");

INSERT INTO parts(manufacturer_id, model, description, part_type_super, part_type_sub) VALUES
(1, "X-12000T", "The standard Triakis head.", "Head", "Standard"),
(2, "H500AE", "The standard Harimau arms.", "Arm", "Standard"),
(3, "PoP 32", "The standard Sylva body.", "Body", "Standard"),
(4, "ElecPack A", "The standard Holobot energy source.", "Energy Source", "Standard"),

(5, "Wheely One", "Lunar Parcs' speedy Robobot wheels.", "Movement", "Wheels"),
(1, "HT-980K", "Triakis' durable Robobot tracks.", "Movement", "Tracks"),
(2, "L500AT", "Harimau's versatile Robobot legs.", "Movement", "Legs"),
(3, "Glide!", "Sylva's cool Robobot hover devices.", "Movement", "Hover Devices"),

(4, "Inferno A", "Holobot's hot flamethrower attachment.", "Attachment", "Offensive"),
(5, "Shieldy One", "Lunar Parcs' protective shield attachment.", "Attachment", "Defensive"),
(1, "GH-10000A", "Triakis' usefull grappling hook attachment.", "Attachment", "Utility"),
(2, "ST100XM", "Harimau's curious stove attachment.", "Attachment", "Miscellaneous");

INSERT INTO head_parts(part_id, durability_score, weight) VALUES
(1, 75, 5000);

INSERT INTO arm_parts(part_id, durability_score, weight) VALUES
(2, 55, 12000);

INSERT INTO energy_source_size(name) VALUES
("A10"),
("A20"),
("A30");

INSERT INTO body_parts(part_id, durability_score, weight, energy_source_size) VALUES
(3, 95, 84000, "A20");

INSERT INTO energy_source_parts(part_id, weight, energy_source_size_name, energy_capacity) VALUES
(4, 3500, "A20", 100000);

INSERT INTO movement_parts(part_id, durability_score, weight, max_speed, energy_draw) VALUES
(5, 40, 3600, 140, 20),
(6, 75, 5500, 60, 45),
(7, 65, 7000, 100, 30),
(8, 30, 2400, 80, 60);

INSERT INTO attachment_parts(part_id, weight, energy_draw) VALUES
(9, 600, 200),
(10, 900, 40),
(11, 100, 80),
(12, 700, 350);

INSERT INTO users(user_name, password) VALUES
-- NOTE: User sign-up will ensure passwords are handled correctly!
("Example User", "password");

INSERT INTO builds(
    user_id,
    head_part_id,
    left_arm_part_id,
    right_arm_part_id,
    body_part_id,
    energy_source_part_id,
    movement_part_id,
    a_attachment_part_id,
    b_attachment_part_id,
    c_attachment_part_id,
    d_attachment_part_id
) VALUES
(1, 1, 1, 1, 1, 1, 1, 1, 2, NULL, NULL);
