#!/usr/bin/env bash
set -e

# === CONFIG ===
# NOTE: This script currently cannot generate models due to Strava API's external $ref links
# that require SSL certificate validation. The existing 55 models in src/main/scala/strava/models/api
# are maintained in git and should be updated manually if the Strava API changes significantly.
# See: https://developers.strava.com/swagger/swagger.json (contains external refs to athlete.json, etc.)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
OPENAPI_GENERATOR_VERSION="7.8.0"
OPENAPI_URL="https://developers.strava.com/swagger/swagger.json"
SWAGGER_LOCAL="/tmp/strava-swagger.json"
MODEL_PACKAGE="strava.models.api"
SRC_MAIN_DIR="${PROJECT_ROOT}/src/main/scala"
MODELS_DIR="${SRC_MAIN_DIR}/strava/models/api"
GENERATOR_JAR="${SCRIPT_DIR}/openapi-generator-cli.jar"
TMP_GEN_DIR="${SCRIPT_DIR}/tmp-strava-gen"

echo "📦 Target Scala package: ${MODEL_PACKAGE}"
echo "📂 Target directory: ${MODELS_DIR}"

# === DEP CHECK ===
for cmd in java curl; do
  if ! command -v $cmd &>/dev/null; then
    echo "❌ Missing dependency: $cmd"
    exit 1
  fi
done

# === DOWNLOAD OPENAPI GENERATOR ===
if [ ! -f "${GENERATOR_JAR}" ]; then
  echo "⬇️ Downloading OpenAPI Generator ${OPENAPI_GENERATOR_VERSION}..."
  curl -L -o "${GENERATOR_JAR}" \
    "https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/${OPENAPI_GENERATOR_VERSION}/openapi-generator-cli-${OPENAPI_GENERATOR_VERSION}.jar"
fi

# === DOWNLOAD SWAGGER ===
if [ ! -f "${SWAGGER_LOCAL}" ]; then
  echo "⬇️ Downloading swagger.json from ${OPENAPI_URL}..."
  curl -s "${OPENAPI_URL}" -o "${SWAGGER_LOCAL}"
else
  echo "📝 Using cached swagger.json from ${SWAGGER_LOCAL}"
fi

SWAGGER_INPUT="${SWAGGER_LOCAL}"

# === GENERATE SCALA MODELS ONLY ===
echo "🚀 Generating Scala models from Strava API..."
# Clean temp dir if it exists from previous run
rm -rf "${TMP_GEN_DIR}"

# Generate models to temp directory
java -jar "${GENERATOR_JAR}" generate \
  -i "${SWAGGER_INPUT}" \
  -g scala-sttp \
  -o "${TMP_GEN_DIR}" \
  --model-package "${MODEL_PACKAGE}" \
  --api-package dummy.skip \
  --invoker-package dummy.skip \
  --global-property models,modelDocs=false,modelTests=false,supportingFiles=false,apiTests=false,apiDocs=false \
  --additional-properties hideGenerationTimestamp=true,enumClassPrefix=true,dateLibrary=java8 \
  --additional-properties modelPropertyNaming=original \
  --skip-validate-spec 2>&1 | tee /tmp/openapi-gen.log | grep -E "INFO|ERROR|WARN" | grep -v "Failed to get the schema name" || true

# === CHECK IF GENERATION SUCCEEDED ===
GENERATED_API_DIR="${TMP_GEN_DIR}/src/main/scala/${MODEL_PACKAGE//./\/}"
if [ -d "${GENERATED_API_DIR}" ] && [ "$(ls -A "${GENERATED_API_DIR}" 2>/dev/null | wc -l)" -gt 0 ]; then
  echo "✅ Generation successful! Found models in ${GENERATED_API_DIR}"
  
  # === REPLACE OLD MODELS WITH NEW ONES ===
  echo "📦 Replacing old models with newly generated ones..."
  if [ -d "${MODELS_DIR}" ]; then
    rm -rf "${MODELS_DIR}"
  fi
  mkdir -p "$(dirname "${MODELS_DIR}")"
  mv "${GENERATED_API_DIR}" "${MODELS_DIR}"
  
  # === CLEAN TEMP DIR ===
  rm -rf "${TMP_GEN_DIR}"
  
  echo ""
  echo "✅ Done! Models updated in: ${MODELS_DIR}"
  echo "📊 Generated files: $(ls -1 "${MODELS_DIR}" | wc -l | tr -d ' ')"
else
  echo ""
  echo "❌ ERROR: Model generation failed or no models were generated!"
  echo "❌ Generated directory: ${GENERATED_API_DIR}"
  echo "❌ Temp directory contents:"
  find "${TMP_GEN_DIR}" -type f 2>/dev/null | head -20 || echo "No files found"
  echo ""
  echo "💡 Keeping existing models unchanged."
  echo "📋 Check log at: /tmp/openapi-gen.log"
  rm -rf "${TMP_GEN_DIR}"
  exit 1
fi
