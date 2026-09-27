#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
main_output="$project_dir/out/main"

mkdir -p "$main_output"
find "$project_dir/src/main/java" -name '*.java' -print > "$project_dir/out/main-sources.txt"
javac --release 17 -encoding UTF-8 -Xlint:all \
  -d "$main_output" \
  @"$project_dir/out/main-sources.txt"
cp "$project_dir/src/main/resources/serverconfig.properties" "$main_output/serverconfig.properties"

printf 'Compiled Java sources to %s\n' "$main_output"
