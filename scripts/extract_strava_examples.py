#!/usr/bin/env python3
import os, requests, json, re
from bs4 import BeautifulSoup

BASE_URL = "https://developers.strava.com/docs/reference/"
OUT_DIR = "src/test/resources/strava"
os.makedirs(OUT_DIR, exist_ok=True)

def clean_name(text):
    return re.sub(r'[^a-z0-9\-]+', '-', text.strip().lower()).strip('-')

def fetch():
    resp = requests.get(BASE_URL)
    resp.raise_for_status()
    return BeautifulSoup(resp.text, "html.parser")

def extract(soup):
    saved = 0
    # look for highlighted code blocks
    selectors = ["div.hljs", "pre", "code"]
    blocks = soup.select(", ".join(selectors))
    for block in blocks:
        text = block.get_text().strip()
        if not (text.startswith("{") or text.startswith("[")):
            continue
        try:
            parsed = json.loads(text)
        except json.JSONDecodeError:
            continue

        header = block.find_previous(["h2", "h3"])
        title = clean_name(header.get_text()) if header else f"example-{saved+1}"
        fname = os.path.join(OUT_DIR, f"{title}.json")
        with open(fname, "w", encoding="utf-8") as f:
            json.dump(parsed, f, indent=2)
        print(f"✓ Saved {fname}")
        saved += 1

    print(f"✅ Total saved: {saved} example JSON files.")

def main():
    soup = fetch()
    extract(soup)

if __name__ == "__main__":
    main()
