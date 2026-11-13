#!/usr/bin/env python3
"""
Prints the plugin-visible API changes between two git revisions.

Every instrumented method of an @Extendable class carries @PublicAPI(from, returns, parameters, locals), the signature
a decorator of a plugin sees, and the fields a plugin reads with getReflectiveFieldValue carry @PublicAPI(from, type)
(see manual/developer/plugins/extendable.md). This tool reads those annotations straight from git (no checkout, no
build, any tag, branch or commit) and lists, per @Extendable class:

    - classes that became / stopped being @Extendable, or moved to another package (a decorator registered for the old
      class is never called again),
    - methods added (harmless to plugins, listed for the release notes),
    - methods removed (a decorator for it never runs again),
    - methods changed: the return type, an argument (name or type, added, removed), a local variable (added, removed,
      retyped) - the things getMethodArgument / getLocalVariable / getCurrentReturn depend on,
    - fields declared as API added, removed (getReflectiveFieldValue fails) or retyped.

    python3 tools/plugin_api_diff.py v1.7.0                    # v1.7.0 -> the working tree's HEAD
    python3 tools/plugin_api_diff.py v1.6.0 v1.7.0             # between two tags
    python3 tools/plugin_api_diff.py v1.7.0 --changelog        # Markdown to paste into the CHANGELOG
    python3 tools/plugin_api_diff.py v1.7.0 --breaking         # only removed / changed (what can break a plugin)
    python3 tools/plugin_api_diff.py v1.7.0 --check            # exit code 1 when something was removed or changed
    python3 tools/plugin_api_diff.py v1.7.0 --worktree         # compare with the files on disk, not with HEAD
    python3 tools/plugin_api_diff.py v1.7.0 --check-from       # also check that `from` of every changed method is
                                                               # newer than the old revision's version (see below)

--check-from: a changed or added method or field must have `from` greater than the version of the old revision (the
project version of its pom.xml, or the tag name), otherwise the annotation was not updated when the signature was.

Fields are opt-in (the classes have thousands of private ones): only those annotated @PublicAPI are compared. A revision
from before @PublicAPI existed (before 1.2.0) has only the class list: methods and fields cannot be compared and the tool
says so.
"""
import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = "marginalia/src/main/java"


def git(*args, check=True):
    return subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True, check=check).stdout


# --------------------------------------------------------------------------------------------------------- reading

def sources(rev):
    """{path: text} of the java files of the application at `rev` (None: the files on disk)."""
    result = {}
    if rev is None:
        for path in (ROOT / SRC).rglob("*.java"):
            result[str(path.relative_to(ROOT))] = path.read_text(encoding="utf-8")
        return result
    # one `git grep` finds the interesting files, one `cat-file --batch` reads them: no process per file
    names = git("grep", "-l", "-E", "@Extendable|@PublicAPI", rev, "--", SRC, check=False).splitlines()
    names = [n.split(":", 1)[1] for n in names if ":" in n]
    if not names:
        return result
    batch = subprocess.run(["git", "cat-file", "--batch"], cwd=ROOT, input="".join(f"{rev}:{n}\n" for n in names).encode(),
                           capture_output=True, check=True).stdout
    pos = 0
    for name in names:
        eol = batch.index(b"\n", pos)
        header = batch[pos:eol].decode().split()
        size = int(header[2])
        result[name] = batch[eol + 1:eol + 1 + size].decode("utf-8")
        pos = eol + 1 + size + 1
    return result


def blank(text):
    """The source with comments, string and char literals and text blocks replaced by spaces (newlines kept), so braces,
    parentheses and annotations inside them do not count."""
    out = []
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        two = text[i:i + 2]
        if two == "//":
            j = text.find("\n", i)
            j = n if j < 0 else j
            out.append(" " * (j - i))
            i = j
        elif two == "/*":
            j = text.find("*/", i + 2)
            j = n if j < 0 else j + 2
            out.append(re.sub(r"[^\n]", " ", text[i:j]))
            i = j
        elif text.startswith('"""', i):
            j = text.find('"""', i + 3)
            j = n if j < 0 else j + 3
            out.append(re.sub(r"[^\n]", " ", text[i:j]))
            i = j
        elif c in "\"'":
            j = i + 1
            while j < n and text[j] != c:
                j += 2 if text[j] == "\\" else 1
            j = min(j + 1, n)
            out.append(c + " " * (j - i - 2) + c if j - i >= 2 else c)
            i = j
        else:
            out.append(c)
            i += 1
    return "".join(out)


def balanced(text, start):
    """Index just after the parenthesis that opens at text[start]."""
    depth = 0
    for i in range(start, len(text)):
        if text[i] == "(":
            depth += 1
        elif text[i] == ")":
            depth -= 1
            if depth == 0:
                return i + 1
    return len(text)


TYPE_DECL = re.compile(r"\b(class|interface|enum|record|@interface)\s+(\w+)")
STRIP_GENERIC = re.compile(r"\s+")


def annotation_values(body):
    """Parses the inside of @PublicAPI( ... ) into (from, return type, [(name, type)], [(name, type)])."""
    since = re.search(r'\bfrom\s*=\s*"([^"]*)"', body)
    ret = re.search(r"\breturns\s*=\s*@APIReturn\s*\(\s*type\s*=\s*([\w.$<>\[\]]+?)\.class\s*\)", body)

    def pairs(tag, annotation):
        found = re.search(r"\b" + tag + r"\s*=\s*\{", body)
        if not found:
            return []
        start = found.end() - 1
        depth, end = 0, start
        for end in range(start, len(body)):
            depth += body[end] == "{"
            depth -= body[end] == "}"
            if depth == 0:
                break
        inner = body[start:end]
        return [(m.group(1), m.group(2)) for m in re.finditer(
            r"@" + annotation + r"\s*\(\s*name\s*=\s*\"([^\"]*)\"\s*,\s*type\s*=\s*([\w.$\[\]]+?)\.class\s*\)", inner)]

    return (since.group(1) if since else "?", ret.group(1) if ret else "?",
            pairs("parameters", "APIParameter"), pairs("locals", "APILocals"))


def field_type(body):
    """The `type = X.class` of @PublicAPI(from = ..., type = X.class) on a field."""
    found = re.search(r"\btype\s*=\s*([\w.$<>\[\]]+?)\.class", body)
    return found.group(1) if found else "?"


def parse(path, text):
    """Returns {class: {"extendable": bool, "methods": {key: Method}}} for the @Extendable classes of one file."""
    code = blank(text)
    package = re.search(r"^\s*package\s+([\w.]+)\s*;", code, re.M)
    package = package.group(1) if package else ""
    # braces: stack of (class name or None)
    declarations = []  # (position of '{', name, extendable)
    for m in TYPE_DECL.finditer(code):
        if m.group(1) == "record" and not re.match(r"\s*(<[^>]*>)?\s*\(", code[m.end():]):
            continue
        brace = code.find("{", m.end())
        semicolon = code.find(";", m.end())
        if brace < 0 or (0 <= semicolon < brace and m.group(1) != "record"):
            continue
        before = code[:m.start()]
        cut = max(before.rfind(";"), before.rfind("{"), before.rfind("}"))
        modifiers = before[cut + 1:]
        declarations.append((brace, m.group(2), bool(re.search(r"@Extendable\b", modifiers))))
    opens = {pos: (name, ext) for pos, name, ext in declarations}

    classes = {}
    stack = []  # (name, extendable) or None per open brace
    methods = []
    fields = []
    events = sorted(set(opens) | {m.start() for m in re.finditer(r"@PublicAPI\b", code)}
                    | {m.start() for m in re.finditer(r"[{}]", code)})
    for pos in events:
        c = code[pos]
        if c == "{":
            stack.append(opens.get(pos))
        elif c == "}":
            if stack:
                stack.pop()
        else:
            owner = [s for s in stack if s][-1:] or None
            paren = code.find("(", pos)
            end = balanced(code, paren)
            body = text[paren + 1:end - 1]  # same offsets; the strings are needed
            values = annotation_values(body)
            # the method name: the identifier before the first '(' that follows, skipping further annotations
            rest = code[end:]
            k = 0
            while True:
                skip = re.match(r"\s*@\w+(\.\w+)*", rest[k:])
                if not skip:
                    break
                k += skip.end()
                if re.match(r"\s*\(", rest[k:]):
                    k = balanced(rest, k + rest[k:].index("("))
            sig = re.match(r"([^(;{=]*?)(\w+)\s*\(", rest[k:], re.S)
            declared_field = re.match(r"([^(;{=]*?)(\w+)\s*[=;]", rest[k:], re.S)
            if owner:
                nested = [s[0] for s in stack if s]
                if sig:
                    methods.append((nested, sig.group(2), values))
                elif declared_field:
                    fields.append((nested, declared_field.group(2), values[0], field_type(body)))
    for nested, name, values in methods:
        qualified = (package + "." if package else "") + ".".join(nested)
        entry = classes.setdefault(qualified, {"extendable": True, "methods": {}, "fields": {}})
        since, ret, params, locs = values
        key = f"{name}({', '.join(t for _, t in params)})"
        entry["methods"][key] = {"name": name, "from": since, "returns": ret, "params": params, "locals": locs}
    for nested, name, since, typ in fields:
        qualified = (package + "." if package else "") + ".".join(nested)
        entry = classes.setdefault(qualified, {"extendable": True, "methods": {}, "fields": {}})
        entry["fields"][name] = {"name": name, "from": since, "type": typ}
    # @Extendable classes without any annotated method (before 1.2.0, or abstract ones)
    stack = []
    for pos in sorted(set(opens) | {m.start() for m in re.finditer(r"[{}]", code)}):
        if code[pos] == "{":
            stack.append(opens.get(pos))
            if opens.get(pos) and opens[pos][1]:
                qualified = (package + "." if package else "") + ".".join(s[0] for s in stack if s)
                classes.setdefault(qualified, {"extendable": True, "methods": {}, "fields": {}})
        elif stack:
            stack.pop()
    return classes


def api(rev):
    """{qualified class: {"methods": {key: method}, "fields": {name: field}}} of all @Extendable classes at `rev`, and
    whether any @PublicAPI exists."""
    result, annotated = {}, False
    for path, text in sources(rev).items():
        if "@Extendable" not in text:
            continue
        for cls, entry in parse(path, text).items():
            result[cls] = {"methods": entry["methods"], "fields": entry["fields"]}
            annotated = annotated or bool(entry["methods"] or entry["fields"])
    return result, annotated


# --------------------------------------------------------------------------------------------------------- comparing

def simple(name):
    return name.rsplit(".", 1)[-1].replace("$", ".")


def describe(m):
    params = ", ".join(f"{t} {n}" for n, t in m["params"])
    return f"{m['returns']} {m['name']}({params})"


def diff_method(old, new):
    changes = []
    if old["returns"] != new["returns"]:
        changes.append(f"return type {old['returns']} -> {new['returns']}")
    oldp, newp = old["params"], new["params"]
    if oldp != newp:
        if [t for _, t in oldp] == [t for _, t in newp]:
            for (on, _), (nn, t) in zip(oldp, newp):
                if on != nn:
                    changes.append(f"argument `{on}` renamed `{nn}` ({t})")
        else:
            changes.append("arguments (" + ", ".join(f"{t} {n}" for n, t in oldp) + ") -> ("
                           + ", ".join(f"{t} {n}" for n, t in newp) + ")")
    oldl, newl = dict(old["locals"]), dict(new["locals"])
    oldset, newset = set(old["locals"]), set(new["locals"])
    for name, t in old["locals"]:
        if (name, t) not in newset:
            if name in newl:
                changes.append(f"local `{name}` {t} -> {newl[name]}")
            else:
                changes.append(f"local `{name}` ({t}) removed")
    for name, t in new["locals"]:
        if (name, t) not in oldset and name not in oldl:
            changes.append(f"local `{name}` ({t}) added")
    return changes


def compare(old, new):
    report = {"classes_added": [], "classes_removed": [], "moved": [], "added": [], "removed": [], "changed": [],
              "fields_added": [], "fields_removed": [], "fields_changed": []}
    by_simple_old = {}
    for c in old:
        by_simple_old.setdefault(simple(c), []).append(c)
    by_simple_new = {}
    for c in new:
        by_simple_new.setdefault(simple(c), []).append(c)
    gone = {c for c in old if c not in new}
    fresh = {c for c in new if c not in old}
    for c in sorted(gone):
        twins = [n for n in by_simple_new.get(simple(c), []) if n in fresh]
        if len(twins) == 1:
            report["moved"].append((c, twins[0]))
            compare_members(old[c], new[twins[0]], report, twins[0])
            fresh.discard(twins[0])
        else:
            report["classes_removed"].append(c)
    for c in sorted(fresh):
        report["classes_added"].append((c, new[c]))
    for c in sorted(set(old) & set(new)):
        compare_members(old[c], new[c], report, c)
    return report


def compare_members(old, new, report, cls):
    compare_methods(cls, old["methods"], new["methods"], report, cls)
    for name in sorted(set(old["fields"]) - set(new["fields"])):
        report["fields_removed"].append((cls, old["fields"][name]))
    for name in sorted(set(new["fields"]) - set(old["fields"])):
        report["fields_added"].append((cls, new["fields"][name]))
    for name in sorted(set(old["fields"]) & set(new["fields"])):
        a, b = old["fields"][name], new["fields"][name]
        if a["type"] != b["type"]:
            report["fields_changed"].append((cls, a, b, [f"type {a['type']} -> {b['type']}"]))


def compare_methods(label, old, new, report, cls):
    # a method renamed with the same arguments is reported as removed + added, which is what a plugin sees
    for key in sorted(set(old) - set(new)):
        report["removed"].append((cls, old[key]))
    for key in sorted(set(new) - set(old)):
        report["added"].append((cls, new[key]))
    for key in sorted(set(old) & set(new)):
        changes = diff_method(old[key], new[key])
        if changes:
            report["changed"].append((cls, old[key], new[key], changes))


# --------------------------------------------------------------------------------------------------------- output

def version_of(rev):
    """The project version at `rev`: a tag v1.7.0 is 1.7.0, otherwise <version> of the root pom.xml at that revision."""
    m = re.fullmatch(r"v?(\d+\.\d+\.\d+)", rev or "")
    if m:
        return m.group(1)
    pom = git("show", f"{rev}:pom.xml", check=False) if rev else (ROOT / "pom.xml").read_text(encoding="utf-8")
    m = re.search(r"<version>([\d.]+)(?:-SNAPSHOT)?</version>", pom)
    return m.group(1) if m else None


def as_tuple(v):
    return tuple(int(p) for p in re.findall(r"\d+", v or "0"))


def by_class(items, pos=0):
    grouped = {}
    for item in items:
        grouped.setdefault(item[pos], []).append(item)
    return grouped


def print_text(report, breaking):
    out = []
    if report["moved"]:
        out.append("MOVED (decorators of the old class are never called):")
        out += [f"  {a}  ->  {b}" for a, b in report["moved"]]
    if report["classes_removed"]:
        out.append("REMOVED / NOT @Extendable ANY MORE:")
        out += [f"  {c}" for c in report["classes_removed"]]
    if report["removed"]:
        out.append("METHODS REMOVED OR RENAMED:")
        for cls, items in by_class(report["removed"]).items():
            out.append(f"  {cls}")
            out += [f"    - {describe(m)}" for _, m in items]
    if report["changed"]:
        out.append("METHODS CHANGED:")
        for cls, items in by_class(report["changed"]).items():
            out.append(f"  {cls}")
            for _, old, new, changes in items:
                out.append(f"    ~ {old['name']}({', '.join(t for _, t in new['params'])})")
                out += [f"        {c}" for c in changes]
    if report["fields_removed"]:
        out.append("FIELDS REMOVED OR RENAMED (getReflectiveFieldValue fails):")
        for cls, items in by_class(report["fields_removed"]).items():
            out.append(f"  {cls}")
            out += [f"    - {f['type']} {f['name']}" for _, f in items]
    if report["fields_changed"]:
        out.append("FIELDS CHANGED:")
        for cls, items in by_class(report["fields_changed"]).items():
            out.append(f"  {cls}")
            for _, old, new, changes in items:
                out.append(f"    ~ {new['name']}: " + "; ".join(changes))
    if not breaking:
        if report["classes_added"]:
            out.append("CLASSES ADDED:")
            for c, members in report["classes_added"]:
                out.append(f"  {c} ({len(members['methods'])} methods, {len(members['fields'])} fields)")
        if report["added"]:
            out.append("METHODS ADDED:")
            for cls, items in by_class([x for x in report["added"] if x[0] not in
                                        {c for c, _ in report["classes_added"]}]).items():
                out.append(f"  {cls}")
                out += [f"    + {describe(m)}" for _, m in items]
        if report["fields_added"]:
            out.append("FIELDS ADDED (declared as API):")
            for cls, items in by_class([x for x in report["fields_added"] if x[0] not in
                                        {c for c, _ in report["classes_added"]}]).items():
                out.append(f"  {cls}")
                out += [f"    + {f['type']} {f['name']}" for _, f in items]
    return "\n".join(out)


def print_changelog(report, old_rev, new_rev):
    out = [f"Plugin API (`@PublicAPI`, {old_rev} -> {new_rev}):"]
    pieces = []
    for a, b in report["moved"]:
        pieces.append(f"`{simple(a)}` moved to `{b}`")
    for c in report["classes_removed"]:
        pieces.append(f"`{simple(c)}` is not `@Extendable` any more (removed)")
    for c, methods in report["classes_added"]:
        pieces.append(f"new `@Extendable` class `{simple(c)}`")
    added = {c for c, _ in report["classes_added"]}
    for cls, items in by_class(report["removed"]).items():
        pieces.append(f"`{simple(cls)}` lost " + ", ".join(f"`{m['name']}`" for _, m in items))
    for cls, items in by_class(report["changed"]).items():
        for _, old, new, changes in items:
            pieces.append(f"`{simple(cls)}.{new['name']}`: " + "; ".join(changes))
    for cls, items in by_class([x for x in report["added"] if x[0] not in added]).items():
        pieces.append(f"`{simple(cls)}` has new " + ", ".join(f"`{m['name']}`" for _, m in items))
    for cls, items in by_class(report["fields_removed"]).items():
        pieces.append(f"`{simple(cls)}` lost the field " + ", ".join(f"`{f['name']}`" for _, f in items))
    for cls, items in by_class(report["fields_changed"]).items():
        for _, old, new, changes in items:
            pieces.append(f"`{simple(cls)}.{new['name']}` (field): " + "; ".join(changes))
    for cls, items in by_class([x for x in report["fields_added"] if x[0] not in added]).items():
        pieces.append(f"`{simple(cls)}` declares the field{'s' if len(items) > 1 else ''} "
                      + ", ".join(f"`{f['name']}`" for _, f in items))
    return out[0] + "\n" + "\n".join(f"- {p}" for p in pieces) if pieces else "No plugin-visible API change."


def check_from(report, old_rev):
    base = version_of(old_rev)
    if not base:
        return ["cannot tell the version of the old revision"]
    problems = []
    for cls, _, new, changes in report["changed"]:
        if as_tuple(new["from"]) <= as_tuple(base):
            problems.append(f"{cls}.{new['name']}: changed ({'; '.join(changes)}) but from = {new['from']} "
                            f"is not newer than {base}")
    for cls, m in report["added"]:
        if as_tuple(m["from"]) <= as_tuple(base):
            problems.append(f"{cls}.{m['name']}: new but from = {m['from']} is not newer than {base}")
    for cls, _, new, changes in report["fields_changed"]:
        if as_tuple(new["from"]) <= as_tuple(base):
            problems.append(f"{cls}#{new['name']}: field changed ({'; '.join(changes)}) but from = {new['from']} "
                            f"is not newer than {base}")
    return problems


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("old", help="the older revision: a tag, branch or commit")
    ap.add_argument("new", nargs="?", default="HEAD", help="the newer revision (default HEAD)")
    ap.add_argument("--worktree", action="store_true", help="compare with the files on disk instead of `new`")
    ap.add_argument("--breaking", action="store_true", help="only removed and changed")
    ap.add_argument("--changelog", action="store_true", help="Markdown for the CHANGELOG")
    ap.add_argument("--check", action="store_true", help="exit 1 when a method or field was removed or changed")
    ap.add_argument("--check-from", action="store_true", help="exit 1 when `from` was not raised for a change")
    args = ap.parse_args()

    for rev in [args.old] + ([] if args.worktree else [args.new]):
        if subprocess.run(["git", "rev-parse", "--verify", "-q", rev + "^{commit}"], cwd=ROOT,
                          capture_output=True).returncode != 0:
            sys.exit(f"unknown revision: {rev}")
    new_rev = None if args.worktree else args.new
    old_api, old_annotated = api(args.old)
    new_api, new_annotated = api(new_rev)
    if not old_annotated:
        print(f"note: {args.old} has no @PublicAPI (introduced in 1.2.0): only classes can be compared, "
              f"every method of a class of {args.new} would count as added, so methods are left out.", file=sys.stderr)
        empty = {"methods": {}, "fields": {}}
        old_api = {c: {k: dict(v) for k, v in new_api[c].items()} if c in new_api else empty for c in old_api}
    report = compare(old_api, new_api)

    label_new = "the working tree" if args.worktree else args.new
    if args.changelog:
        print(print_changelog(report, args.old, label_new))
    else:
        print(f"Plugin API {args.old} -> {label_new}: {len(old_api)} -> {len(new_api)} @Extendable classes")
        text = print_text(report, args.breaking)
        print(text if text else "no plugin-visible change")
    problems = check_from(report, args.old) if args.check_from else []
    for p in problems:
        print("from: " + p, file=sys.stderr)
    broken = bool(report["removed"] or report["changed"] or report["classes_removed"] or report["moved"]
                  or report["fields_removed"] or report["fields_changed"])
    if (args.check and broken) or problems:
        sys.exit(1)


if __name__ == "__main__":
    main()
