# Dev Seeding

This backend now uses centralized JSON seed files and local image assets.

## Seed Sources

- Seeder code: `src/main/java/com/group2/backend/seed/DatabaseSeeder.java`
- Central artwork data: `src/main/resources/seed/artworks.json`
- Central account data: `src/main/resources/seed/accounts.json`
- Central tag catalog: `src/main/resources/seed/tags.json`
- Image assets: `src/main/resources/seed/artworks/`

## Data Model

`artworks.json` contains for every artwork:

- stable artwork id (`aw-xxx`)
- MET object id
- title, artist, objectDate, year
- source URL
- image paths
- preassigned creator username
- views and price
- full tag list

`accounts.json` contains for every account:

- account identity fields (`username`, `email`, `password`, `roles`)
- `createdArtworkIds`
- `purchasedArtworkIds`
- `likedArtworkIds`

`tags.json` is the canonical list of all allowed tags. Seeder validation fails if any artwork references a missing tag.

## Asset Folder Rules

- Artwork folders live under `seed_assets/artworks/`.
- Folders contain only image files.
- Per-folder `metadata.json` files are no longer used.

## Rebuild Central Seed Files

Use the generator script:

```powershell
".venv/Scripts/python.exe" scripts/build_seed_data.py
```

This script:

- moves `downloaded_artworks/` to `seed_assets/artworks/` when needed
- consolidates data into `artworks.json`, `accounts.json`, and `tags.json`
- removes legacy per-folder metadata files
- validates tag references

## Run Seeder

The seeder runs automatically when the `dev` profile is active.

```powershell
$env:SPRING_PROFILES_ACTIVE='dev'; .\mvnw.cmd spring-boot:run
```
