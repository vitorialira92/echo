import os
import re
import sys
import json
import time

import requests
import lyricsgenius
import musicbrainzngs
from dotenv import load_dotenv

OUTPUT_FILE = "music_seed.json"
USER_AGENT = "echo-seed-generator/1.0 (https://example.com)"
DEEZER_API = "https://api.deezer.com"

MAX_LYRICS = 10000
TRUNCATION_MARK = "... [TRUNCATED]"

INCLUDE_RECORD_TYPES = ("album", "ep", "single")

REQUEST_DELAY = 0.2

GENRE_RULES = [
    (["k-pop", "k pop", "kpop", "korean"], "KPOP"),
    (["trap"], "TRAP"),
    (["reggaeton"], "LATIN"),
    (["latin"], "LATIN"),
    (["hip hop", "hip-hop", "hiphop"], "HIPHOP"),
    (["rap"], "RAP"),
    (["deep house", "progressive house", "house"], "HOUSE"),
    (["techno"], "TECHNO"),
    (["edm", "electronic", "electronica", "electro"], "ELECTRONIC"),
    (["metalcore", "heavy metal", "metal"], "METAL"),
    (["indie"], "INDIE"),
    (["r&b", "rnb", "r and b"], "RNB"),
    (["lo-fi", "lofi"], "LOFI"),
    (["ambient"], "AMBIENT"),
    (["gospel"], "GOSPEL"),
    (["mpb"], "MPB"),
    (["pagode"], "PAGODE"),
    (["samba"], "SAMBA"),
    (["sertanejo"], "SERTANEJO"),
    (["country"], "COUNTRY"),
    (["folk"], "FOLK"),
    (["reggae"], "REGGAE"),
    (["punk"], "PUNK"),
    (["jazz"], "JAZZ"),
    (["blues"], "BLUES"),
    (["classical", "orchestra", "opera", "classique"], "CLASSICAL"),
    (["disco"], "DISCO"),
    (["funk"], "FUNK"),
    (["soul"], "SOUL"),
    (["rock"], "ROCK"),
    (["pop"], "POP"),
]


def map_genre(genre_name):
    g = (genre_name or "").lower()
    for keywords, enum_value in GENRE_RULES:
        for kw in keywords:
            if kw in g:
                return enum_value
    return None


def pick_genre(genres):
    for g in genres or []:
        mapped = map_genre(g)
        if mapped:
            return mapped
    return "POP"


COUNTRY_ENUM = {
    "BR", "US", "CA", "GB", "FR", "DE", "IT", "ES", "NL", "SE", "NO",
    "JP", "KR", "CN", "IN", "TH", "PH", "AU", "MX", "AR", "CO",
}

COUNTRY_NAME_TO_CODE = {
    "brazil": "BR",
    "united states": "US",
    "united states of america": "US",
    "usa": "US",
    "canada": "CA",
    "united kingdom": "GB",
    "uk": "GB",
    "england": "GB",
    "scotland": "GB",
    "wales": "GB",
    "great britain": "GB",
    "france": "FR",
    "germany": "DE",
    "italy": "IT",
    "spain": "ES",
    "netherlands": "NL",
    "sweden": "SE",
    "norway": "NO",
    "japan": "JP",
    "south korea": "KR",
    "korea": "KR",
    "republic of korea": "KR",
    "china": "CN",
    "india": "IN",
    "thailand": "TH",
    "philippines": "PH",
    "australia": "AU",
    "mexico": "MX",
    "argentina": "AR",
    "colombia": "CO",
}


def country_from_name(name):
    if not name:
        return None
    low = name.strip().lower()
    if len(low) == 2 and low.upper() in COUNTRY_ENUM:
        return low.upper()
    return COUNTRY_NAME_TO_CODE.get(low)


def country_from_musicbrainz(name):
    try:
        result = musicbrainzngs.search_artists(artist=name, limit=1)
        artists = result.get("artist-list", [])
        if not artists:
            return None
        artist = artists[0]
        code = artist.get("country")
        if code and code in COUNTRY_ENUM:
            return code
        for key in ("area", "begin-area"):
            area = artist.get(key) or {}
            mapped = country_from_name(area.get("name"))
            if mapped:
                return mapped
        return None
    except Exception as exc:
        print(f"  WARN musicbrainz '{name}': {exc}", file=sys.stderr)
        return None


def country_from_wikipedia(name):
    try:
        from urllib.parse import quote
        url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + quote(name.replace(" ", "_"))
        resp = requests.get(url, headers={"User-Agent": USER_AGENT}, timeout=10)
        if not resp.ok:
            return None
        extract = (resp.json().get("extract") or "").lower()
        for country_name, code in COUNTRY_NAME_TO_CODE.items():
            if country_name in extract:
                return code
        return None
    except Exception as exc:
        print(f"  WARN wikipedia '{name}': {exc}", file=sys.stderr)
        return None


def resolve_country(name):
    return country_from_musicbrainz(name) or country_from_wikipedia(name) or "US"


EDITION_KEYWORDS = [
    "deluxe", "clean", "explicit", "expanded", "anniversary", "remaster",
    "remastered", "edition", "version", "reissue", "bonus", "live", "acoustic",
    "instrumental", "commentary", "karaoke", "sped up", "slowed", "demo",
    "mono", "stereo", "tour", "spilled", "complete",
]


def is_edition(name):
    low = (name or "").lower()
    return any(kw in low for kw in EDITION_KEYWORDS)


def normalize_base(name):
    base = (name or "").lower()
    base = re.sub(r"\(.*?\)", "", base)
    base = re.sub(r"\[.*?\]", "", base)
    base = re.sub(
        r"\s*[-:]\s*(deluxe|clean|explicit|expanded|anniversary|remaster|remastered|"
        r"edition|version|reissue|bonus|live|acoustic|instrumental|complete).*$",
        "",
        base,
    )
    base = re.sub(r"\s+", " ", base).strip()
    return base or (name or "").lower()


def select_main_editions(raw_albums):
    chosen = {}
    for album in sorted(raw_albums, key=lambda a: a.get("release_date", "")):
        name = album.get("title", "")
        key = (album.get("record_type", ""), normalize_base(name))
        if key not in chosen:
            chosen[key] = album
        elif is_edition(chosen[key].get("title", "")) and not is_edition(name):
            chosen[key] = album
    return sorted(chosen.values(), key=lambda a: a.get("release_date", ""))

def clean_title_for_search(title):
    t = re.sub(r"\(.*?\)", "", title or "")
    t = re.sub(r"\[.*?\]", "", t)
    t = re.sub(r"\s*-\s*(remix|remaster|live|.*version|.*edit|sped up|slowed).*$", "", t, flags=re.I)
    t = re.sub(r"\s*(feat\.|ft\.|featuring|with)\s.*$", "", t, flags=re.I)
    t = re.sub(r"\s+", " ", t).strip()
    return t or (title or "")


def norm_title(title):
    return clean_title_for_search(title).lower()


def clean_lyrics(text):
    if not text:
        return None
    text = text.replace("\r", "")
    lines = text.split("\n")
    if lines and lines[0].rstrip().lower().endswith("lyrics"):
        lines = lines[1:]
    text = "\n".join(lines).strip()
    text = re.sub(r"\d*Embed\s*$", "", text).strip()
    text = re.sub(r"You might also like\s*$", "", text).strip()
    if len(text) > MAX_LYRICS:
        text = text[: MAX_LYRICS - len(TRUNCATION_MARK)].rstrip() + TRUNCATION_MARK
    return text or None


def fetch_lyrics(genius, title, artist_name):
    if genius is None:
        return None
    try:
        song = genius.search_song(clean_title_for_search(title), artist_name)
        time.sleep(0.2)
        if not song or not song.lyrics:
            return None
        return clean_lyrics(song.lyrics)
    except Exception as exc:
        print(f"    WARN lyrics '{title}': {exc}", file=sys.stderr)
        return None

def deezer_get(url, params=None):
    if not url.startswith("http"):
        url = DEEZER_API + url
    for attempt in range(3):
        resp = requests.get(url, params=params, headers={"User-Agent": USER_AGENT}, timeout=15)
        resp.raise_for_status()
        data = resp.json()
        if isinstance(data, dict) and data.get("error"):
            err = data["error"]
            if err.get("code") == 4 and attempt < 2:
                print("  WARN Deezer quota; aguardando 5s...", file=sys.stderr)
                time.sleep(5)
                continue
            raise RuntimeError(f"Deezer error: {err}")
        time.sleep(REQUEST_DELAY)
        return data
    raise RuntimeError("Deezer: limite de cota (code 4) apos varias tentativas")


def deezer_search_artist(name):
    data = deezer_get("/search/artist", {"q": name, "limit": 1})
    items = data.get("data", [])
    return items[0] if items else None


def deezer_artist_albums(artist_id):
    out = []
    data = deezer_get(f"/artist/{artist_id}/albums", {"limit": 100})
    out.extend(data.get("data", []))
    nxt = data.get("next")
    while nxt:
        data = deezer_get(nxt)
        out.extend(data.get("data", []))
        nxt = data.get("next")
    return out


def deezer_album(album_id):
    return deezer_get(f"/album/{album_id}")


def album_tracks_from_detail(detail):
    tracks_obj = detail.get("tracks") or {}
    tracks = list(tracks_obj.get("data", []))
    nxt = tracks_obj.get("next")
    while nxt:
        data = deezer_get(nxt)
        tracks.extend(data.get("data", []))
        nxt = data.get("next")
    return tracks

def build_song(genius, track, artist_name):
    title = track.get("title")
    return {
        "title": title,
        "trackNumber": track.get("track_position"),
        "durationSeconds": int(track.get("duration") or 0),
        "lyrics": fetch_lyrics(genius, title, artist_name),
        "spotifyUrl": None,
        "explicit": bool(track.get("explicit_lyrics", False)),
    }


def build_album(genius, album_summary, skip_titles=None, seen_titles=None):
    detail = deezer_album(album_summary["id"])
    name = detail.get("title") or album_summary.get("title")
    release_date = detail.get("release_date") or album_summary.get("release_date") or ""
    year = int(release_date[:4]) if release_date[:4].isdigit() else None
    cover_url = (detail.get("cover_xl") or detail.get("cover_big")
                 or album_summary.get("cover_xl") or album_summary.get("cover_big"))
    genre = pick_genre([g.get("name") for g in (detail.get("genres") or {}).get("data", [])])
    artist_name = (detail.get("artist") or {}).get("name") or ""

    songs = []
    for track in album_tracks_from_detail(detail):
        try:
            nt = norm_title(track.get("title") or "")
            if skip_titles is not None and nt in skip_titles:
                continue
            songs.append(build_song(genius, track, artist_name))
            if seen_titles is not None:
                seen_titles.add(nt)
        except Exception as exc:
            print(f"    ERROR song '{track.get('title')}': {exc}", file=sys.stderr)

    return {
        "name": name,
        "year": year,
        "genre": genre,
        "coverUrl": cover_url,
        "spotifyUrl": None,
        "songs": songs,
    }


def build_artist(genius, query):
    artist = deezer_search_artist(query)
    if not artist:
        print(f"WARN: artista nao encontrado: {query}", file=sys.stderr)
        return None

    artist_id = artist["id"]
    name = artist.get("name")
    image_url = artist.get("picture_xl") or artist.get("picture_big")
    country = resolve_country(name)

    print(f"-> {name} [{country}] ...", file=sys.stderr)

    raw = [a for a in deezer_artist_albums(artist_id)
           if a.get("record_type") in INCLUDE_RECORD_TYPES]
    releases = select_main_editions(raw)
    full_albums = [r for r in releases if r.get("record_type") == "album"]
    singles = [r for r in releases if r.get("record_type") != "album"]

    seen_titles = set()
    albums = []

    for release in full_albums:
        try:
            built = build_album(genius, release, seen_titles=seen_titles)
            albums.append(built)
            print(f"   + {built['name']} [album, {len(built['songs'])} faixas]", file=sys.stderr)
        except Exception as exc:
            print(f"  ERROR album '{release.get('title')}': {exc}", file=sys.stderr)

    for release in singles:
        try:
            built = build_album(genius, release, skip_titles=seen_titles, seen_titles=seen_titles)
            if built["songs"]:
                albums.append(built)
                print(f"   + {built['name']} [{release.get('record_type')}, {len(built['songs'])} faixas]",
                      file=sys.stderr)
            else:
                print(f"   - pulado (faixas ja em album): {release.get('title')}", file=sys.stderr)
        except Exception as exc:
            print(f"  ERROR release '{release.get('title')}': {exc}", file=sys.stderr)

    return {
        "name": name,
        "country": country,
        "imageUrl": image_url,
        "albums": albums,
    }

def read_artist_names():
    names = []
    print("Digite os artistas (linha vazia para terminar):", file=sys.stderr)
    while True:
        try:
            line = input("Artist: ")
        except EOFError:
            break
        line = line.strip()
        if not line:
            break
        names.append(line)
    return names


def main():
    load_dotenv()

    musicbrainzngs.set_useragent("echo-seed-generator", "1.0", "echo@example.com")

    genius_token = os.getenv("GENIUS_ACCESS_TOKEN")
    genius = None
    if genius_token:
        genius = lyricsgenius.Genius(
            genius_token,
            timeout=15,
            retries=3,
            remove_section_headers=True,
            skip_non_songs=True,
        )
        genius.excluded_terms = ["(Remix)", "(Live)", "(Demo)"]
    else:
        print("AVISO: GENIUS_ACCESS_TOKEN nao definido; as letras ficarao como null.", file=sys.stderr)

    names = read_artist_names()

    artists = []
    for name in names:
        try:
            built = build_artist(genius, name)
            if built:
                artists.append(built)
        except Exception as exc:
            print(f"ERROR artista '{name}': {exc}", file=sys.stderr)

    data = {"artists": artists}
    with open(OUTPUT_FILE, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=2, ensure_ascii=False)

    print(f"\nOK: '{OUTPUT_FILE}' gerado com {len(artists)} artista(s).", file=sys.stderr)


if __name__ == "__main__":
    main()