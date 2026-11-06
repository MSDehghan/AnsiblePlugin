"""Collect ansible-doc JSON for the modules installed in the current environment."""

import json
from pathlib import Path
import subprocess
import sys

# Bounds the command length even when many collections are installed.
BATCH_SIZE = 1000


def ansible_doc(*args):
    return subprocess.check_output(["ansible-doc", "-t", "module", *args], text=True)


def collect(output):
    # Listing filenames avoids parsing module summaries, which can contain invalid
    # description types. Plain output also works with Ansible's tagged path values.
    names = [line.split(None, 1)[0] for line in ansible_doc("--list_files").splitlines() if line.strip()]
    if not names:
        raise RuntimeError("ansible-doc did not discover any installed modules")
    print(f"Collecting documentation for {len(names)} installed modules", flush=True)

    modules = {}
    for offset in range(0, len(names), BATCH_SIZE):
        batch = names[offset:offset + BATCH_SIZE]
        documents = json.loads(ansible_doc("--json", *batch))
        missing = [name for name in batch if not documents.get(name, {}).get("doc")]
        if missing:
            raise RuntimeError("Missing module documentation: " + ", ".join(missing))
        modules.update(documents)

    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(modules, ensure_ascii=False), encoding="utf-8")
    print(f"Collected documentation for {len(modules)} modules: {output}")


if __name__ == "__main__":
    collect(Path(sys.argv[1]))
