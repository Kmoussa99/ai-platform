#!/usr/bin/env bash
# Ingère tous les fichiers de BDD/ dans le RAG via l'API document-service.
# Mapping sous-dossier → collection. Tous les docs en accessLevel L1 (public).
set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BDD="$ROOT/BDD"
GATEWAY="${GATEWAY:-http://localhost:8000}"
KEYCLOAK="${KEYCLOAK:-http://localhost:8080}"
USER="${KC_USER:-admin.demo}"
PASS="${KC_PASS:-admin123}"

# 1. Récupérer un token
echo "🔑 Login Keycloak ($USER)..."
TOKEN=$(curl -sf -X POST "$KEYCLOAK/realms/ai-platform/protocol/openid-connect/token" \
  -d "client_id=ai-platform-frontend" \
  -d "grant_type=password" \
  -d "username=$USER" \
  -d "password=$PASS" | python3 -c "import sys,json;print(json.load(sys.stdin)['access_token'])")
[[ -z "$TOKEN" ]] && { echo "❌ Login failed"; exit 1; }
echo "✅ Token len=${#TOKEN}"

# 2. Pour chaque fichier
total=0; ok=0; ko=0
while IFS= read -r -d '' f; do
  total=$((total+1))
  rel="${f#$BDD/}"
  collection=$(dirname "$rel")
  [[ "$collection" == "." ]] && collection="general"
  name=$(basename "$f")
  printf "📄 [%2d] %-60s → collection=%-25s ... " "$total" "$name" "$collection"
  http_code=$(curl -s -o /tmp/upload-resp.json -w "%{http_code}" --max-time 180 -X POST "$GATEWAY/api/v1/documents/upload" \
    -H "Authorization: Bearer $TOKEN" \
    -F "file=@$f" \
    -F "collection=$collection" \
    -F "accessLevel=L1")
  if [[ "$http_code" == "200" ]]; then
    id=$(python3 -c "import sys,json;print(json.load(open('/tmp/upload-resp.json')).get('id','?'))" 2>/dev/null)
    echo "✅ HTTP 200 id=$id"
    ok=$((ok+1))
  else
    echo "❌ HTTP $http_code  body=$(head -c 200 /tmp/upload-resp.json)"
    ko=$((ko+1))
  fi
done < <(find "$BDD" -type f \( -name "*.pdf" -o -name "*.md" -o -name "*.docx" -o -name "*.txt" \) -print0 | sort -z)

echo
echo "📊 Résultat : $ok/$total uploadés ($ko erreurs)"
echo
echo "🔍 Vérification indexation Qdrant (peut prendre quelques min en arrière-plan)..."
sleep 3
curl -s http://localhost:6333/collections/knowledge_internal | python3 -c "import sys,json;d=json.load(sys.stdin)['result'];print(f'  Points indexes: {d.get(\"points_count\",\"?\")}, status: {d.get(\"status\",\"?\")}')"

