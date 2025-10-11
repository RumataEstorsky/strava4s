#!/usr/bin/env bash

set -e

OPENAPI_URL="https://developers.strava.com/swagger/swagger.json"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
EXAMPLES_DIR="${PROJECT_ROOT}/src/test/resources/strava"

# === DEP CHECK ===
for cmd in curl jq; do
  if ! command -v $cmd &>/dev/null; then
    echo "❌ Missing dependency: $cmd"
    exit 1
  fi
done

# === DOWNLOAD SWAGGER SPEC ===
echo "⬇️ Downloading Strava swagger.json..."
curl -s "${OPENAPI_URL}" -o /tmp/strava-swagger.json

# === PREPARE EXAMPLES DIRECTORY ===
echo "🧹 Preparing examples directory..."
# Safety check: only modify if the path contains "strava" or "examples" to avoid accidental deletion
if [[ "${EXAMPLES_DIR}" == *"/strava"* ]] || [[ "${EXAMPLES_DIR}" == *"/examples"* ]]; then
  mkdir -p "${EXAMPLES_DIR}"
  echo "✅ Directory ready (existing files will be preserved or updated)"
else
  echo "⚠️  Safety check failed: EXAMPLES_DIR doesn't look like a test resources directory."
  exit 1
fi

# === EXTRACT RESPONSE EXAMPLES SAFELY ===
echo "🧪 Extracting response examples..."
jq -r '
  .paths // {}
  | to_entries[]
  | .key as $path
  | select(.value | type == "object")
  | .value
  | to_entries[]
  | .key as $method
  | .value as $operation
  | select($operation | type == "object")
  | $operation.operationId as $opId
  | $operation.summary as $summary
  | $operation.responses // {}
  | select(. != null and type == "object")
  | to_entries[]
  | .key as $status
  | select(.value | type == "object")
  # Support both Swagger 2.0 (.examples) and OpenAPI 3.0 (.content.*.example)
  | (.value.examples["application/json"] // .value.content["application/json"].example // null) as $ex
  | select($ex != null)
  # Convert operationId from camelCase to kebab-case for descriptive naming
  | ($opId | ascii_downcase | gsub("(?<a>[a-z])(?<b>[A-Z])"; "\(.a)-\(.b)") | ascii_downcase) as $opIdKebab
  # Create descriptive name from summary or path
  | (if $summary then ($summary | ascii_downcase | gsub("[^a-z0-9]+"; "-") | gsub("^-+|-+$"; "")) else ($path | gsub("^/|/$"; "") | gsub("[^a-z0-9]+"; "-")) end) as $description
  # Combine into format: {description}-{operationId}.json
  | ($description + "-" + $opIdKebab) as $fileName
  | [($ex | @json), $fileName]
  | @tsv
' /tmp/strava-swagger.json | while IFS=$'\t' read -r example_json file_name; do
  file="${EXAMPLES_DIR}/${file_name}.json"
  echo "$example_json" | jq . > "$file"
  echo "✅ Saved: $file"
done

# === EXTRACT SCHEMA EXAMPLES SAFELY ===
echo "🧪 Extracting schema examples..."
# Support both OpenAPI 3.0 (components.schemas) and Swagger 2.0 (definitions)
SCHEMA_COUNT=$(jq -r '
  ((.components.schemas // .definitions // {})
  | to_entries[]
  | select(.value != null and (.value | type == "object"))
  | select(.value.example != null)
  | [.key, (.value.example | @json)]
  | @tsv)
' /tmp/strava-swagger.json | wc -l | tr -d ' ')

if [ "$SCHEMA_COUNT" -gt 0 ]; then
  jq -r '
    ((.components.schemas // .definitions // {})
    | to_entries[]
    | select(.value != null and (.value | type == "object"))
    | select(.value.example != null)
    | [.key, (.value.example | @json)]
    | @tsv)
  ' /tmp/strava-swagger.json | while IFS=$'\t' read -r schema_name example_json; do
    file="${EXAMPLES_DIR}/${schema_name}_schema.json"
    echo "$example_json" | jq . > "$file"
    echo "✅ Saved: $file"
  done
else
  echo "ℹ️  No schema examples found in swagger file"
fi

# === NOTE ABOUT MISSING ENDPOINTS ===
echo ""
echo "ℹ️  Note: Some endpoints don't have examples in swagger.json (stats, routes, uploads)"
echo "ℹ️  These files are maintained separately in git. Run 'git checkout HEAD -- src/test/resources/strava/' to restore if needed."

echo ""
echo "🎉 DONE!"
echo "✅ JSON examples saved in: ${EXAMPLES_DIR}/"
TOTAL_FILES=$(ls -1 "${EXAMPLES_DIR}" | wc -l | tr -d ' ')
echo "📊 Total files: ${TOTAL_FILES}"
