#!/usr/bin/env python3
"""Build the app's bundled question pack (assets/questions/core_questions.json).

The app loads ONE master JSON file shaped { "packId": ..., "questions": [...] }.
This script assembles that master file from two sources:

  1. The standalone validated question bank (2,500 questions), located by default
     at ../magic-kingdom-trivia-question-bank (override with MK_QUESTION_BANK).
  2. tools/original_seed_questions.json — the app's original seed questions,
     retained because they are not exact duplicates of the standalone bank.

Conversion rules:
  * Output uses the app's 8-field schema. The standalone bank's `subtopic` field
    is intentionally omitted, because no screen or engine logic consumes it and
    PROJECT.md calls for avoiding unnecessary complexity. The standalone bank
    remains the system of record for subtopics.
  * Seed questions whose normalized text exactly matches a bank question are
    dropped (bank takes precedence), so the master file has no duplicate prompts.
  * Ordering is deterministic: bank questions first (category, then difficulty,
    then id), followed by the retained seed questions in their original order.

Usage:
    python3 tools/build_question_pack.py            # writes the asset
    python3 tools/build_question_pack.py --check    # verify asset is up to date

The result is validated separately by the JVM test
`ProductionQuestionBankTest` and by the standalone bank's own validator.
"""

import argparse
import json
import os
import re
import sys

APP_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSET = os.path.join(APP_ROOT, "app/src/main/assets/questions/core_questions.json")
SEEDS = os.path.join(APP_ROOT, "tools/original_seed_questions.json")
DEFAULT_BANK = os.environ.get(
    "MK_QUESTION_BANK",
    os.path.abspath(os.path.join(APP_ROOT, "..", "magic-kingdom-trivia-question-bank")),
)
PACK_ID = "core-v1"

APP_FIELDS = ["id", "question", "answers", "correctAnswerIndex",
              "category", "difficulty", "explanation", "sourceTitle"]

# Deterministic bank file order.
BANK_ORDER = [
    "disney_animation", "pixar", "disney_princesses", "live_action_disney",
    "disney_parks", "disney_songs", "marvel", "star_wars",
]
DIFF_ORDER = ["easy.json", "medium.json", "hard.json"]


def normalize(text):
    t = re.sub(r"[^\w\s]", " ", text.lower().strip())
    return " ".join(w for w in t.split() if w not in {"a", "an", "the"})


def to_app_schema(q):
    return {k: q[k] for k in APP_FIELDS}


def build(bank_dir):
    bank = []
    for folder in BANK_ORDER:
        for fname in DIFF_ORDER:
            path = os.path.join(bank_dir, "questions", folder, fname)
            with open(path, encoding="utf-8") as fh:
                data = json.load(fh)
            data.sort(key=lambda q: q["id"])
            bank.extend(to_app_schema(q) for q in data)

    bank_norms = {normalize(q["question"]) for q in bank}

    with open(SEEDS, encoding="utf-8") as fh:
        seeds = json.load(fh)

    retained, dropped = [], []
    for q in seeds:
        if normalize(q["question"]) in bank_norms:
            dropped.append(q["id"])
        else:
            retained.append(to_app_schema(q))

    questions = bank + retained
    return questions, len(bank), retained, dropped


def render(questions):
    return json.dumps({"packId": PACK_ID, "questions": questions},
                      indent=2, ensure_ascii=False) + "\n"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bank", default=DEFAULT_BANK, help="Path to the standalone question bank")
    ap.add_argument("--check", action="store_true",
                    help="Exit non-zero if the asset differs from a fresh build")
    args = ap.parse_args()

    if not os.path.isdir(args.bank):
        sys.exit(f"Question bank not found: {args.bank}\n"
                 f"Set MK_QUESTION_BANK or pass --bank <path>.")

    questions, bank_count, retained, dropped = build(args.bank)
    content = render(questions)

    if args.check:
        current = open(ASSET, encoding="utf-8").read() if os.path.exists(ASSET) else ""
        if current != content:
            sys.exit("core_questions.json is OUT OF DATE. Run: python3 tools/build_question_pack.py")
        print(f"OK: core_questions.json is up to date ({len(questions)} questions).")
        return

    with open(ASSET, "w", encoding="utf-8") as fh:
        fh.write(content)

    print(f"Wrote {ASSET}")
    print(f"  bank questions   : {bank_count}")
    print(f"  retained seeds   : {len(retained)}")
    print(f"  dropped seeds    : {len(dropped)} (exact duplicates of the bank: {dropped})")
    print(f"  total questions  : {len(questions)}")


if __name__ == "__main__":
    main()
