#!/usr/bin/env python3
"""Checks the parts of the dataset build (work/harness/export/book.json) against the rules of the Style lorebook entry and the skeleton.

    python3 tools/workset_check.py                 # all parts
    python3 tools/workset_check.py 6 25            # parts 6 to 25
    python3 tools/workset_check.py --harness work/harness-run3

What it flags (the author reads the text; this finds what to look at):
  - foreign text the model wrote itself: kana / kanji, romaji words, Czech letters outside names, italic phrases (glosses, thoughts in another language)
  - "said something", "murmured something", "whispered the command": speech hidden from the translators (they only translate quoted speech)
  - language cues ("in Japanese") counted per part, and "said in English" habits
  - the length against the word range of the beat
  - the words per day of the timeline against the daily goal of 500
"""
import argparse
import collections
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

ap = argparse.ArgumentParser()
ap.add_argument("first", nargs="?", type=int, default=1)
ap.add_argument("last", nargs="?", type=int, default=100)
ap.add_argument("--harness", default=os.path.join(ROOT, "work", "harness"))
ap.add_argument("--skeleton", default=os.path.join(ROOT, "work", "dataset", "beats.json"))
args = ap.parse_args()

book = json.load(open(os.path.join(args.harness, "export", "book.json"), encoding="utf-8"))
skeleton = json.load(open(args.skeleton, encoding="utf-8"))
by_uuid = {m["uuid"]: m for m in book["messages"]}
chain, u = [], book.get("activeLeafUuid")
while u:
    chain.append(by_uuid[u])
    u = by_uuid[u].get("parentUuid")
chain.reverse()

NAMES = {"Tomáš", "Hájek", "Mizuki", "Hayashida", "Steing", "Paskal", "Mangel", "Frecht", "Kestim", "Roid", "Saellen", "Aelmoria", "Lyrasil", "Silvakhor", "Kaelthal",
         "Stein-krog", "Zint-karn", "Vat-veg", "Zorg-ort", "Lyrshara", "Vrakht", "Aethel-Vael", "Prague", "Praha"}
KANA = re.compile(r"[぀-ヿ一-鿿]+")
CZECH = re.compile(r"[ěščřžýáíéúůťďňĚŠČŘŽÝÁÍÉÚŮŤĎŇ]")
ROMAJI = re.compile(r"\b(?:desu|masu|nihongo|wakaru|sumimasen|arigat[oō]u?|kaerou|kaeritai|hontou|ikiteru|mitai|daijoubu|hai|iie|baka|ne\?|gomen)\b", re.I)
HIDDEN = re.compile(r"\b(?:said|murmured|muttered|whispered|spoke|replied|answered)\s+(?:something|a few words|a word|a phrase|the command|the words)\b", re.I)
CUE = re.compile(r"\bin (?:broken )?(?:English|Japanese|Czech|Vrakht|Aethel-Vael)\b")
HABIT = re.compile(r"\b(?:he|she) said in English\b")

problems = 0
total_words = 0
per_day = collections.defaultdict(int)
for number, part in enumerate(chain, 1):
    if number < args.first or number > args.last:
        continue
    ec = part["extendedContent"]
    text = ec.get("response", "")
    words = part.get("wordCount", len(text.split()))
    total_words += words
    beat = skeleton["beats"][number - 1] if number <= len(skeleton["beats"]) else None
    flags = []
    if KANA.search(text):
        flags.append("kana/kanji %s" % KANA.findall(text)[:3])
    stripped = text
    for n in NAMES:
        stripped = stripped.replace(n, "")
    if CZECH.search(stripped):
        flags.append("czech letters")
    if ROMAJI.search(text):
        flags.append("romaji %s" % ROMAJI.findall(text)[:3])
    italics = re.findall(r"\*[^*\n]{3,}\*", text)
    if italics:
        flags.append("italics %s" % [i[:40] for i in italics[:2]])
    if HIDDEN.search(text):
        flags.append("hidden speech %s" % [m.group(0) for m in HIDDEN.finditer(text)][:2])
    habit = len(HABIT.findall(text))
    if habit > 1:
        flags.append("'said in English' x%d" % habit)
    cues = len(CUE.findall(text))
    if beat:
        lo, hi = beat["words"]
        if words < lo * 0.75 or words > hi * 1.25:
            flags.append("length %d vs %d-%d" % (words, lo, hi))
        per_day[beat["day"]] += words
    problems += bool(flags)
    print("%3d  %4d words  cues %-2d  %s" % (number, words, cues, "; ".join(flags) if flags else "ok"))

print()
print("parts checked: %d, with something to look at: %d, words: %d" % (min(args.last, len(chain)) - args.first + 1, problems, total_words))
if per_day:
    days = sorted(per_day)
    print("words per day (day: words):", ", ".join("%d: %d" % (d, per_day[d]) for d in days), " - days under 500:", sum(1 for d in days if per_day[d] < 500))
sys.exit(0)
