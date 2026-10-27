-- Optional migration for the example Food_Item rows from FCinemaDB_seed.sql.
-- Execute after pulling the committed assets from GitHub.
-- Only fills currently empty image_url values; no existing upload link is overwritten.
UPDATE dbo.Food_Item
SET image_url = 'assets/images/food/popcorn.png'
WHERE item_name = N'Bắp rang bơ' AND (image_url IS NULL OR LTRIM(RTRIM(image_url)) = '');

UPDATE dbo.Food_Item
SET image_url = 'assets/images/food/coca-cola.png'
WHERE item_name = N'Coca Cola' AND (image_url IS NULL OR LTRIM(RTRIM(image_url)) = '');

-- If you want a project-managed combo image, move the existing
-- assets/images/food/combo-couple.png to assets/images/combos/combo-couple.png
-- and commit it before enabling this statement:
-- UPDATE dbo.Combo SET image_url = 'assets/images/combos/combo-couple.png'
-- WHERE combo_name = N'Combo Couple' AND image_url IS NULL;
