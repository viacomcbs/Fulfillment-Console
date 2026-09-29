#!/usr/bin/env bash
# BSD-30019 — In-sprint automation: 3 Synergy sessions + 1 combined email (22 scenarios).
set -euo pipefail

EMAIL="${1:?Usage: ./scripts/run-bsd-30019.sh you@paramount.com [UAT]}"
ENV="${2:-UAT}"

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

AGGREGATE_DIR="$REPO_ROOT/test-output/bsd-30019-aggregate"
rm -rf "$AGGREGATE_DIR"
mkdir -p "$AGGREGATE_DIR"

run_session() {
  local name="$1"
  local suite="$2"
  echo ""
  echo "=== $name ==="
  mvn -B test \
    "-DsuiteXmlFile=$suite" \
    "-Dsystem.test.testenvironment=$ENV" \
    "-Dsystem.test.sendreportautoemails=false"
  cp "$REPO_ROOT/target/surefire-reports/testng-results.xml" \
    "$AGGREGATE_DIR/${name}-testng-results.xml"
}

run_session "session1-orders-perfilter" \
  "src/test/resources/insprint-automation/BSD-30019_OrdersPerFilter_ProdServerSuite.xml"
run_session "session2-lineitems-perfilter" \
  "src/test/resources/insprint-automation/BSD-30019_LineItemsPerFilter_ProdServerSuite.xml"
run_session "session3-story-validation" \
  "src/test/resources/insprint-automation/BSD-30019_StoryValidation_ProdServerSuite.xml"

echo ""
echo "=== Sending ONE combined email ==="
mvn -B test \
  "-DsuiteXmlFile=src/test/resources/insprint-automation/BSD-30019_SendCombinedEmail_ProdServerSuite.xml" \
  "-Dsystem.test.testenvironment=$ENV" \
  "-Dsystem.test.sendreportemailaddress=$EMAIL" \
  "-Dsystem.test.aggregateresultsdir=test-output/bsd-30019-aggregate"

echo "Done. Expect ONE email with 22 scenarios (8 + 8 + 6)."
