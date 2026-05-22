#!/usr/bin/env bash
# Télécharge les corpus officiels (CGI, Code travail, SYSCOHADA, CNPS) dans BDD/
set -u

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BDD="$ROOT/BDD"
mkdir -p "$BDD"/{fiscalite_ci,droit_travail_ci,comptabilite,paie_ci}

# UA pour contourner les 403 sur certains sites .gouv.ci
UA="Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15"

fetch() {
  local url="$1" out="$2" label="$3"
  curl -sSL --connect-timeout 20 --max-time 180 -A "$UA" -o "$out" "$url"
  local code=$?
  local size=$(wc -c < "$out" 2>/dev/null || echo 0)
  if [[ $code -ne 0 || $size -lt 5000 ]]; then
    printf "❌ %-40s (size=%s, exit=%s)\n" "$label" "$size" "$code"
    rm -f "$out"
    return 1
  fi
  # vérif PDF (magic %PDF)
  if ! head -c 4 "$out" | grep -q "%PDF"; then
    printf "⚠️  %-40s (pas un PDF, size=%s)\n" "$label" "$size"
    rm -f "$out"
    return 1
  fi
  printf "✅ %-40s %s bytes\n" "$label" "$size"
}

echo "===== Fiscalité ====="
fetch "https://www.dgbf.ci/wp-content/uploads/2024/11/Annexe1-Annexe-Fiscale.pdf" \
      "$BDD/fiscalite_ci/annexe-fiscale-2025.pdf" "annexe-fiscale-2025"
fetch "https://www.dgi.gouv.ci/assets/documents/IMPOTS%20ET%20TAXES%20EN%20COTE%20D'IVOIRE%20.pdf" \
      "$BDD/fiscalite_ci/impots-et-taxes-ci.pdf" "impots-et-taxes"

echo "===== Code du Travail ====="
fetch "https://www.famille.gouv.ci/public/front/docs/RCI-Code-2015-travail.pdf" \
      "$BDD/droit_travail_ci/code-du-travail-2015.pdf" "code-travail-2015"
fetch "https://agp.africanlii.org/fr/akn/ci/act/2015/532/fra@2023-08-10/source.pdf" \
      "$BDD/droit_travail_ci/code-du-travail-2023-amende.pdf" "code-travail-2023-amende"
fetch "https://cotedivoirepaie.ci/wp-content/uploads/2021/08/Nouveau-Code-du-travail-Loi-N2015-532-Code-du-Travail.pdf" \
      "$BDD/droit_travail_ci/code-du-travail-2015-bis.pdf" "code-travail-2015-bis"
fetch "https://www.economie-ivoirienne.ci/sites/default/files/sites/default/files/inline-files/Ordonnance%20n%C2%B02021-902%20du%2022%20d%C3%A9cembre%202021%20modifiant%20la%20loi%20n%C2%B02015-532%20du%2020%20juillet%202015%20portant%20Code%20du%20travail.pdf" \
      "$BDD/droit_travail_ci/ordonnance-2021-902.pdf" "ordonnance-2021-902"

echo "===== SYSCOHADA ====="
fetch "https://www.droit-afrique.com/uploads/OHADA-Acte-uniforme-2017-droit-comptable-information-financiere.pdf" \
      "$BDD/comptabilite/ohada-acte-uniforme-comptable-2017.pdf" "ohada-comptable-2017"

echo "===== CNPS ====="
fetch "https://www.cnps.ci/wp-content/uploads/2020/08/Recouvrement.pdf" \
      "$BDD/paie_ci/cnps-recouvrement.pdf" "cnps-recouvrement"
fetch "https://www.cnps.ci/wp-content/uploads/2023/01/NOUVEAU-PLAFOND-DES-COTISATIONS-SOCIALES-DE-LA-CNPS.pdf" \
      "$BDD/paie_ci/cnps-nouveau-plafond-2023.pdf" "cnps-nouveau-plafond-2023"

echo
echo "===== Récap ====="
find "$BDD" -type f -name "*.pdf" -exec ls -lh {} \; | awk '{printf "%-8s %s\n",$5,$NF}'
echo
echo "Total fichiers : $(find "$BDD" -type f -name '*.pdf' | wc -l | tr -d ' ')"
echo "Taille totale  : $(du -sh "$BDD" | awk '{print $1}')"

