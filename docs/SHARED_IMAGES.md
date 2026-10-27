# Shared images in FCinema

This branch uses **project-managed static assets** so team members can pull the same pictures from GitHub. It does **not** upload files to GitHub from the running website.

## Workflow

1. Add JPG, PNG, GIF, or WEBP images to `src/main/webapp/assets/images/movies/`, `src/main/webapp/assets/images/food/`, or `src/main/webapp/assets/images/combos/`.
2. Commit/push the image files to GitHub and let teammates pull them.
3. On the Movie / Food Item / Combo form, enter the project-relative path, **without a leading slash**, e.g. `assets/images/movies/avatar.jpg`.
4. Save the record. SQL Server stores the relative path in `poster_url` or `image_url`. Team members must also have the corresponding database rows / image paths (Git does not synchronize SQL Server).
5. Visit `http://localhost:8080/<context>/assets/images/food/popcorn.png` (replace `<context>` with your Tomcat application context) to verify the asset is served.

## Folder mapping

| Record | Source folder | Example value in SQL Server |
| --- | --- | --- |
| Movie.poster_url | `assets/images/movies/` | `assets/images/movies/avatar.jpg` |
| Food_Item.image_url | `assets/images/food/` | `assets/images/food/popcorn.png` |
| Combo.image_url | `assets/images/combos/` | `assets/images/combos/combo-couple.png` |

The repository currently has example images in `assets/images/food/`; the movie/combo folders start empty. **Existing `http(s)` movie poster URLs and previously saved local food/combo image paths can still display for backwards compatibility, but they are not shared assets.** To share them reliably, copy the original image into the appropriate project folder, commit it, and update its database path.

The Movie form still accepts a separate trailer URL, which is for video and is unaffected by the image-storage change.

## Important constraints

- Movie posters use a path field; Food/Combo file-upload inputs have been replaced with shared-asset path inputs. Choosing an arbitrary file from a user's computer does **not** automatically put it in Git.
- Never store `C:\\Users\\...\\file.jpg`, `file://...`, or `localhost` machine URLs in shared DB rows.
- Avoid overwriting a filename while someone still uses a cached copy; use a new filename when changing an image.
- If you need Manager-side *live file upload* with instant access for the whole team, use Cloudinary/S3 or another shared image server instead. GitHub folders are suitable for a fixed/demo asset catalog, not collaborative live uploads.
- SQL Server `image_url` fields should contain the relative path, not the operating-system file path. Existing data is **not automatically migrated**; optional migration examples are in `database/shared_images_seed.sql`.
