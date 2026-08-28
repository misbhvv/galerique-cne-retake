import os
import re
import json
import time
import requests
from pathlib import Path

BASE_URL = "https://collectionapi.metmuseum.org/public/collection/v1"
OUTPUT_DIR = Path("./downloaded_artworks")
MAX_IMAGES_PER_ARTWORK = 10
MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024  # 5 MB
SEARCH_QUERY = "painting"
MAX_ARTWORKS = 50  # pas aan zoals je wilt

session = requests.Session()
session.headers.update({
    "User-Agent": "school-project-artwork-downloader/1.0"
})


def slugify(text: str) -> str:
    text = text.strip()
    text = re.sub(r'[<>:"/\\\\|?*]', "", text)
    text = re.sub(r"\s+", " ", text)
    return text[:120] or "untitled"


def get_json(url: str, params=None):
    resp = session.get(url, params=params, timeout=30)
    resp.raise_for_status()
    return resp.json()


def head_content_length(url: str):
    try:
        resp = session.head(url, allow_redirects=True, timeout=20)
        resp.raise_for_status()
        cl = resp.headers.get("Content-Length")
        return int(cl) if cl and cl.isdigit() else None
    except Exception:
        return None


def download_if_small_enough(url: str, filepath: Path, max_bytes: int) -> bool:
    size = head_content_length(url)
    if size is not None and size > max_bytes:
        print(f"SKIP > 5MB: {url}")
        return False

    try:
        with session.get(url, stream=True, timeout=60) as resp:
            resp.raise_for_status()

            total = 0
            with open(filepath, "wb") as f:
                for chunk in resp.iter_content(chunk_size=8192):
                    if not chunk:
                        continue
                    total += len(chunk)
                    if total > max_bytes:
                        f.close()
                        filepath.unlink(missing_ok=True)
                        print(f"SKIP tijdens download > 5MB: {url}")
                        return False
                    f.write(chunk)

        return True
    except Exception as e:
        print(f"Download mislukt: {url} -> {e}")
        filepath.unlink(missing_ok=True)
        return False


def search_object_ids(query: str):
    data = get_json(
        f"{BASE_URL}/search",
        params={
            "q": query,
            "hasImages": "true"
        }
    )
    return data.get("objectIDs") or []


def get_object(object_id: int):
    return get_json(f"{BASE_URL}/objects/{object_id}")


def collect_image_urls(obj: dict):
    urls = []

    # lagere resolutie eerst voor kleinere bestanden
    if obj.get("primaryImageSmall"):
        urls.append(obj["primaryImageSmall"])
    elif obj.get("primaryImage"):
        urls.append(obj["primaryImage"])

    for img in obj.get("additionalImages", []):
        if img not in urls:
            urls.append(img)

    return urls[:MAX_IMAGES_PER_ARTWORK]


def save_metadata(folder: Path, obj: dict, downloaded_files: list[str]):
    metadata = {
        "objectID": obj.get("objectID"),
        "title": obj.get("title"),
        "artist": obj.get("artistDisplayName"),
        "objectDate": obj.get("objectDate"),
        "isPublicDomain": obj.get("isPublicDomain"),
        "files": downloaded_files
    }
    with open(folder / "metadata.json", "w", encoding="utf-8") as f:
        json.dump(metadata, f, ensure_ascii=False, indent=2)


def main():
    OUTPUT_DIR.mkdir(exist_ok=True)

    object_ids = search_object_ids(SEARCH_QUERY)
    print(f"Gevonden object IDs: {len(object_ids)}")

    processed = 0

    for object_id in object_ids:
        if processed >= MAX_ARTWORKS:
            break

        try:
            obj = get_object(object_id)
        except Exception as e:
            print(f"Object ophalen mislukt {object_id}: {e}")
            continue

        if not obj.get("isPublicDomain"):
            continue

        title = obj.get("title", "").strip()
        if not title:
            continue

        image_urls = collect_image_urls(obj)
        if not image_urls:
            continue

        folder_name = f"{obj['objectID']}_{slugify(title)}"
        artwork_dir = OUTPUT_DIR / folder_name
        artwork_dir.mkdir(exist_ok=True)

        downloaded = []
        image_index = 1

        for url in image_urls:
            ext = ".jpg"
            filename = f"{slugify(title)}_{image_index}{ext}"
            filepath = artwork_dir / filename

            ok = download_if_small_enough(url, filepath, MAX_IMAGE_SIZE_BYTES)
            if ok:
                downloaded.append(filename)
                image_index += 1

            if image_index > MAX_IMAGES_PER_ARTWORK:
                break

        if downloaded:
            save_metadata(artwork_dir, obj, downloaded)
            print(f"OK: {title} -> {len(downloaded)} images")
            processed += 1
        else:
            try:
                artwork_dir.rmdir()
            except OSError:
                pass

        time.sleep(0.1)  # beetje vriendelijk blijven naar de API


if __name__ == "__main__":
    main()