#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
test_output="$project_dir/out/test"

"$project_dir/scripts/compile.sh"
mkdir -p "$test_output"
find "$project_dir/src/test/java" -name '*.java' -print > "$project_dir/out/test-sources.txt"
javac --release 17 -encoding UTF-8 -Xlint:all \
  -cp "$project_dir/out/main" \
  -d "$test_output" \
  @"$project_dir/out/test-sources.txt"
java -Djava.awt.headless=true \
  -cp "$project_dir/out/main:$test_output" \
  dev.jingfan.jchat.ChatroomSmokeTest
