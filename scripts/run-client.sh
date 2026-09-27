#!/usr/bin/env sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
"$project_dir/scripts/compile.sh"
cd "$project_dir"
exec java -cp "$project_dir/out/main" dev.jingfan.jchat.client.ClientMain
