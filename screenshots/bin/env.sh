# Shared settings of the screenshot environment. Source it: `. screenshots/bin/env.sh`
SHOT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]:-$0}")/.." && pwd)"
REPO_ROOT="$(cd "$SHOT_ROOT/.." && pwd)"
export SHOT_ROOT REPO_ROOT
export SHOT_HOME="$SHOT_ROOT/.data"            # used as -Duser.home, data ends up in .data/.marginalia
export SHOT_PORT=8099                          # the application
export SHOT_LLM_PORT=8098                      # the fake inference server
export SHOT_URL="http://localhost:$SHOT_PORT"
export SHOT_WAR="$REPO_ROOT/marginalia/target/marginalia-1.9.0.war"
# Maven must run on the project's JDK (the brew default is older than the class files the build makes)
[ -d "$HOME/Library/Java/JavaVirtualMachines/corretto-26.0.2.1/Contents/Home" ] && export JAVA_HOME="$HOME/Library/Java/JavaVirtualMachines/corretto-26.0.2.1/Contents/Home"
