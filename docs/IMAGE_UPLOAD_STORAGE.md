# F-Cinema image uploads

All images uploaded from the manager screens are stored inside the project source tree:

- Food and drinks: `src/main/webapp/assets/images/food/`
- Combos: `src/main/webapp/assets/images/combo/`
- Movie posters: `src/main/webapp/assets/images/movies/`

The database stores a relative path such as `assets/images/food/<uuid>.png`, not a machine-specific absolute path. The `ProjectImageServlet` serves newly uploaded files directly from the source tree during development and falls back to resources packaged in the WAR.

## Sharing uploaded images with teammates

Uploading a file creates it in the local project folder. Git does not push files automatically, so commit and push the new image files along with your code:

```bash
git status
git add src/main/webapp/assets/images
git commit -m "Add uploaded F-Cinema images"
git push
```

Other developers must pull the commit containing those files. They also need to use the same database (or a database whose image path columns contain the corresponding relative paths).

Supported formats are JPG/JPEG, PNG and GIF, up to 5 MB per upload. Existing images previously saved under a user's home directory are not moved automatically; re-upload them through the updated form if they also need to be shared through Git.
