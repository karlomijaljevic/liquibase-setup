SELECT setval('person_id_seq', COALESCE((SELECT MAX(id) FROM person), 0), true);
