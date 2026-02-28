#!/usr/bin/env python3

from pathlib import Path
from shutil import copyfile


def main() -> None:
    script_dir = Path(__file__).resolve().parent
    project_dir = script_dir.parent
    source_file = project_dir / "element-templates" / "telegram-outbound-connector.json"
    target_file = project_dir / "connectors" / "telegram-connector.json"

    if not source_file.exists():
        raise FileNotFoundError(f"Expected generated outbound template at {source_file}")

    target_file.parent.mkdir(parents=True, exist_ok=True)
    copyfile(source_file, target_file)


if __name__ == "__main__":
    main()
