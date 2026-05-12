-- =============================================================================
-- HOEM — Seed data
-- Run manually after first startup: docker-compose exec postgresql psql -U hoem -d hoem -f /scripts/seed-data.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Global pantry categories
-- -----------------------------------------------------------------------------
INSERT INTO pantry.categories (id, name, icon, is_global, household_id) VALUES
  (gen_random_uuid(), 'Dairy',           'milk',       TRUE, NULL),
  (gen_random_uuid(), 'Bakery',          'bread',      TRUE, NULL),
  (gen_random_uuid(), 'Fruit & Veg',     'apple',      TRUE, NULL),
  (gen_random_uuid(), 'Meat & Fish',     'drumstick',  TRUE, NULL),
  (gen_random_uuid(), 'Frozen',          'snowflake',  TRUE, NULL),
  (gen_random_uuid(), 'Pasta & Rice',    'bowl',       TRUE, NULL),
  (gen_random_uuid(), 'Canned Goods',    'can',        TRUE, NULL),
  (gen_random_uuid(), 'Sauces & Oils',   'bottle',     TRUE, NULL),
  (gen_random_uuid(), 'Snacks',          'cookie',     TRUE, NULL),
  (gen_random_uuid(), 'Drinks',          'cup',        TRUE, NULL),
  (gen_random_uuid(), 'Cleaning',        'spray',      TRUE, NULL),
  (gen_random_uuid(), 'Personal Care',   'soap',       TRUE, NULL),
  (gen_random_uuid(), 'Other',           'box',        TRUE, NULL)
ON CONFLICT DO NOTHING;

-- -----------------------------------------------------------------------------
-- Global recipe catalogue (sample — extend with more recipes)
-- -----------------------------------------------------------------------------

-- Recipe 1: Spaghetti Bolognese
WITH r AS (
  INSERT INTO menu.recipes (id, household_id, name, description, category, difficulty, prep_time_min, cook_time_min, servings, is_global)
  VALUES (gen_random_uuid(), NULL, 'Spaghetti Bolognese', 'Classic Italian meat sauce with spaghetti', 'dinner', 'easy', 15, 45, 4, TRUE)
  RETURNING id
)
INSERT INTO menu.recipe_steps (recipe_id, step_number, instruction, duration_min)
SELECT r.id, 1, 'Heat olive oil in a large pan over medium heat. Add diced onion and cook for 5 minutes until soft.', 5 FROM r
UNION ALL
SELECT r.id, 2, 'Add minced garlic and cook for 1 minute. Add ground beef and cook until browned, breaking it up with a spoon.', 10 FROM r
UNION ALL
SELECT r.id, 3, 'Add canned tomatoes, tomato paste, oregano, salt and pepper. Simmer for 30 minutes, stirring occasionally.', 30 FROM r
UNION ALL
SELECT r.id, 4, 'Cook spaghetti according to package instructions. Drain and serve with the sauce on top.', 10 FROM r;

-- Recipe 2: Greek Salad
WITH r AS (
  INSERT INTO menu.recipes (id, household_id, name, description, category, difficulty, prep_time_min, cook_time_min, servings, is_global)
  VALUES (gen_random_uuid(), NULL, 'Greek Salad', 'Fresh Mediterranean salad with feta and olives', 'lunch', 'easy', 10, 0, 2, TRUE)
  RETURNING id
)
INSERT INTO menu.recipe_steps (recipe_id, step_number, instruction)
SELECT r.id, 1, 'Chop tomatoes, cucumber and red onion into bite-sized pieces. Place in a large bowl.' FROM r
UNION ALL
SELECT r.id, 2, 'Add olives and crumbled feta cheese.' FROM r
UNION ALL
SELECT r.id, 3, 'Drizzle with olive oil, add dried oregano, salt and pepper. Toss gently and serve.' FROM r;

-- Recipe 3: Vegetable Stir Fry
WITH r AS (
  INSERT INTO menu.recipes (id, household_id, name, description, category, difficulty, prep_time_min, cook_time_min, servings, is_global)
  VALUES (gen_random_uuid(), NULL, 'Vegetable Stir Fry', 'Quick and healthy Asian-inspired stir fry', 'dinner', 'easy', 10, 15, 2, TRUE)
  RETURNING id
)
INSERT INTO menu.recipe_steps (recipe_id, step_number, instruction, duration_min)
SELECT r.id, 1, 'Prepare all vegetables: slice bell peppers, broccoli florets, julienne carrots, slice courgette.', 10 FROM r
UNION ALL
SELECT r.id, 2, 'Heat sesame oil in a wok or large frying pan over high heat. Add garlic and ginger, stir for 30 seconds.', 1 FROM r
UNION ALL
SELECT r.id, 3, 'Add the harder vegetables first (carrots, broccoli) and stir fry for 3 minutes. Add the rest and stir fry for another 3 minutes.', 6 FROM r
UNION ALL
SELECT r.id, 4, 'Add soy sauce and a splash of water. Stir to coat. Serve over rice or noodles.', 2 FROM r;
