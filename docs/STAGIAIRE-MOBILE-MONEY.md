# 🎓 Guide du stagiaire — Plateforme IA « Mobile Money Assistant »

> **Pour qui ?** Un stagiaire **sans compétence technique préalable**.
> **Objectif :** Construire (en copiant cette plateforme) un assistant IA spécialisé **Mobile Money** (Orange Money, MTN MoMo, Moov Money, Wave) pour la Côte d'Ivoire / Afrique de l'Ouest.
> **Durée estimée :** 6 à 8 semaines à temps plein.
> **Méthode :** À **chaque étape**, tu poses tes questions à l'assistant (le chat IA de cette plateforme, ou ChatGPT / Copilot). Il t'explique, te corrige, te débloque. **Tu n'es jamais seul.**

---

## 📜 Règles d'or (à lire AVANT de coder)

1. **Tu n'as pas besoin de tout comprendre tout de suite.** Tu avances étape par étape.
2. **Copie d'abord, comprends ensuite.** Le code de référence est dans ce dépôt `ai-platform`. Tu vas le dupliquer puis l'adapter au Mobile Money.
3. **Pose une question = à chaque blocage de plus de 15 minutes.** Format de question idéale :
   - Ce que je veux faire
   - Ce que j'ai fait
   - Le message d'erreur **complet** (copier-coller, pas une capture)
   - Ce que je pense que ça veut dire
4. **Commit Git tous les jours**, même si ça ne marche pas. Message clair (`feat: ajoute parser SMS Orange Money`).
5. **Ne supprime jamais un dossier sans demander.** En cas de doute → `git status` puis demande.

---

## 🎯 Le sujet : « MoMo Assistant »

Un assistant qui répond aux questions des **commerçants, agents et utilisateurs Mobile Money** :

- « Quels sont les frais d'un transfert Orange Money de 50 000 FCFA vers un non-abonné ? »
- « Comment annuler une transaction Wave envoyée par erreur ? »
- « Quel est le plafond mensuel d'un compte MTN MoMo niveau 2 ? »
- « Génère-moi un rapport de commissions agent pour le mois de mars. »
- « Lis ce SMS de confirmation et dis-moi si c'est une arnaque. »

### Briques fonctionnelles à livrer

| Module | Description |
|---|---|
| **Chat RAG** | Réponses fondées sur la doc officielle des opérateurs (grilles tarifaires, CGU, règlement BCEAO). |
| **Détection de fraude SMS** | L'utilisateur colle un SMS, l'IA dit « légitime / suspect » + pourquoi. |
| **Calculateur de frais** | Saisir montant + opérateur + type d'opération → renvoie frais exacts. |
| **Rapports agent** | Upload d'un relevé CSV → synthèse commissions / volumes. |
| **Admin** | Gestion utilisateurs (agent, superviseur, client), quotas, audit. |

---

## 🗺️ Vue d'ensemble du parcours

```
Semaine 1 : Comprendre + installer
Semaine 2 : Cloner et faire tourner ai-platform à l'identique
Semaine 3 : Collecter la base documentaire Mobile Money
Semaine 4 : Adapter le RAG (ingestion, prompts)
Semaine 5 : Ajouter le module fraude SMS
Semaine 6 : Calculateur de frais + rapports CSV
Semaine 7 : Brander « MoMo Assistant » (UI, logo, textes)
Semaine 8 : Tests, démo, documentation
```

---

# 🧭 Étape par étape

## ✅ Étape 0 — Préparer ton poste (Jour 1, ~3h)

**But :** avoir tous les outils installés.

### À installer (Mac ou Linux recommandé)

```bash
# 1. Homebrew (si Mac)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 2. Git, Docker Desktop, Node 20, Java 21, Maven
brew install --cask docker
brew install git node@20 maven openjdk@21

# 3. Ollama (pour faire tourner l'IA en local)
brew install ollama

# 4. Un éditeur : VS Code OU IntelliJ IDEA Community
brew install --cask visual-studio-code
```

### Vérifie

```bash
git --version           # >= 2.40
docker --version        # >= 24
node --version          # v20.x
java --version          # 21
mvn --version           # >= 3.9
ollama --version
```

### Ouvre un compte

- **GitHub** (avec ton vrai nom)
- Demande à ton tuteur l'accès au dépôt `ai-platform`

> 🤖 **Demande à l'IA :** « Explique-moi simplement ce que fait Docker, comme si j'avais 12 ans. »

---

## ✅ Étape 1 — Comprendre l'architecture (Jour 2, ~4h)

**But :** savoir ce que tu vas construire, sans encore coder.

### Lis dans cet ordre

1. `README.md` (racine) → vue d'ensemble
2. `docs/adr/001-microservices.md` → pourquoi on coupe en plusieurs services
3. `docs/adr/002-rag-stack.md` → comment l'IA va « lire » les docs
4. `docs/adr/003-token-security.md` → comment on protège l'accès

### Schéma à mémoriser

```
Utilisateur (navigateur)
        ↓
   [ Frontend Angular ]      ← l'interface (ce que voit l'humain)
        ↓
   [ Gateway ]               ← le portier qui vérifie le ticket
        ↓
   ┌────────────┬────────────┬──────────────┐
   ↓            ↓            ↓              ↓
[Chat]    [Documents]   [Tokens]      [Payroll]   ← services métier (Java Spring Boot)
   ↓            ↓
   └────→ [ ML-service Python ] ←──── Ollama (le cerveau IA local)
                ↓
        [ PostgreSQL + Qdrant ]  ← mémoire / vectoriel
```

> 🤖 **Demande à l'IA :** « Explique-moi le rôle de chaque service dans ai-platform avec une analogie de restaurant. »

---

## ✅ Étape 2 — Faire tourner ai-platform tel quel (Semaine 1 fin)

**But :** voir le projet original fonctionner avant de le modifier.

```bash
cd ~/IdeaProjects
git clone <url-du-depot> ai-platform
cd ai-platform

# 1. Démarrer Ollama et télécharger le petit modèle
ollama serve &           # tourne en arrière-plan
ollama pull qwen2.5:1.5b
ollama pull nomic-embed-text

# 2. Démarrer toute la plateforme
docker compose up -d --build

# 3. Vérifier (attendre ~2 min)
docker compose ps
```

Ouvre dans le navigateur :
- http://localhost:4200 → l'app
- Connecte-toi avec `admin.demo` / `admin123`

✅ **Critère de réussite :** tu poses une question dans le chat et tu reçois une réponse.

> 🤖 **Si ça ne marche pas :** copie-colle la sortie de `docker compose logs <nom-du-service>` dans le chat IA et demande de l'aide.

---

## ✅ Étape 3 — Créer ton propre dépôt « momo-assistant » (Semaine 2, J1)

**But :** ne pas casser le projet de référence.

```bash
cd ~/IdeaProjects
cp -R ai-platform momo-assistant
cd momo-assistant
rm -rf .git
git init
git add .
git commit -m "chore: clone initial depuis ai-platform"

# Crée un dépôt vide sur GitHub puis :
git remote add origin <url-de-ton-nouveau-depot>
git push -u origin main
```

### Renomme le projet partout

Fichiers à éditer (cherche/remplace « ai-platform » → « momo-assistant ») :
- `pom.xml` (racine)
- `docker-compose.yml`, `docker-compose.prod.yml`
- `README.md`
- `frontend/package.json` → `"name": "momo-assistant"`
- `frontend/src/index.html` → `<title>MoMo Assistant</title>`

> 🤖 **Demande à l'IA :** « Voici mon `docker-compose.yml`, propose-moi les renommages exhaustifs pour passer de ai-platform à momo-assistant. »

---

## ✅ Étape 4 — Collecter la base documentaire Mobile Money (Semaine 3)

**But :** rassembler **les vraies données** que l'IA va apprendre.

### Crée la structure

```bash
mkdir -p BDD/momo/{orange_money,mtn_momo,moov_money,wave,reglementation_bceao,fraude}
```

### Sources à télécharger (légalement, docs publiques)

| Dossier | À mettre dedans |
|---|---|
| `orange_money/` | Grille tarifaire PDF Orange Money CI, CGU, plafonds par niveau KYC |
| `mtn_momo/` | Idem MTN Mobile Money |
| `moov_money/` | Idem Moov Money |
| `wave/` | Grille tarifaire Wave (souvent en ligne — fais des PDF/print) |
| `reglementation_bceao/` | Instructions BCEAO sur monnaie électronique, lutte anti-blanchiment |
| `fraude/` | Articles de presse, alertes officielles, exemples de SMS frauduleux (anonymisés) |

### Crée aussi des fiches Markdown synthétiques

Exemple `BDD/momo/orange_money/frais-2025.md` :

```markdown
# Frais Orange Money Côte d'Ivoire — 2025

## Transfert vers abonné Orange Money
| Tranche (FCFA)   | Frais |
|------------------|-------|
| 500 – 5 000      | 50    |
| 5 001 – 25 000   | 200   |
| ...              | ...   |

## Retrait en cash
...
```

> 🤖 **Demande à l'IA :** « Voici la grille tarifaire Wave que j'ai copiée du site, mets-la en tableau Markdown propre. »

✅ **Critère :** au moins **30 documents** au total, dont **10 fiches Markdown synthétiques** que tu as toi-même rédigées.

---

## ✅ Étape 5 — Adapter le script d'ingestion (Semaine 4, J1-2)

**But :** dire à l'IA « lis le dossier `BDD/momo/` au lieu de `BDD/` ».

Fichier : `scripts/ingest-bdd.sh`

```bash
# Avant
SOURCE_DIR="./BDD"

# Après
SOURCE_DIR="./BDD/momo"
COLLECTION_NAME="momo_knowledge"
```

Fichier : `ml-service/rag/pipeline.py` → cherche le nom de collection Qdrant et remplace par `momo_knowledge`.

Puis lance :

```bash
./scripts/ingest-bdd.sh
```

> 🤖 **Demande à l'IA :** « Explique-moi ligne par ligne ce que fait `ingest-bdd.sh`. »

✅ **Critère :** dans le chat, tu demandes « Quels sont les frais Wave pour un transfert de 10 000 FCFA ? » → la réponse cite ton document.

---

## ✅ Étape 6 — Adapter les prompts (Semaine 4, J3-5)

**But :** que l'IA se présente comme **« MoMo Assistant »** et reste dans le sujet.

Cherche dans `ml-service/` les fichiers contenant `system_prompt` ou `SYSTEM_PROMPT`. Remplace par :

```text
Tu es MoMo Assistant, expert des services Mobile Money en Afrique de l'Ouest
(Orange Money, MTN MoMo, Moov Money, Wave). Tu réponds en français clair,
en citant tes sources (grilles tarifaires, CGU, réglementation BCEAO).
Si la question ne concerne pas le Mobile Money, dis-le poliment et redirige.
Si tu n'es pas sûr d'un chiffre, dis-le et invite à vérifier sur le site officiel.
```

> 🤖 **Demande à l'IA :** « Améliore ce system prompt pour qu'il refuse de donner des conseils d'évasion fiscale ou de blanchiment. »

---

## ✅ Étape 7 — Module détection de fraude SMS (Semaine 5)

**But :** ajouter une page « Analyser un SMS ».

### Côté frontend (Angular)

1. Crée un dossier `frontend/src/app/fraud/`
2. Crée 4 fichiers : `fraud.component.ts`, `.html`, `.css`, `.spec.ts`
3. Inspire-toi de `frontend/src/app/tokens/` (même structure)
4. Ajoute la route dans `frontend/src/app/app.routes.ts`
5. Ajoute un item dans la sidebar (`core/sidebar/sidebar.component.ts`) avec icône `bi-shield-exclamation`

### Côté backend (réutilise le chat)

Pas besoin de nouveau service ! Tu envoies juste un prompt spécial au LLM :

```text
Analyse ce SMS reçu par un utilisateur de Mobile Money.
Dis si c'est :
- LÉGITIME (transaction réelle)
- SUSPECT (probable arnaque / phishing)
- INCERTAIN

Liste les indices (numéro court officiel, fautes, urgence, lien externe, etc.)
Réponds en JSON : { "verdict": ..., "confiance": 0-100, "indices": [...] }

SMS : """
<contenu collé par l'utilisateur>
"""
```

> 🤖 **Demande à l'IA :** « Donne-moi 10 exemples de SMS frauduleux Mobile Money typiques pour tester mon module. »

✅ **Critère :** tu colles 3 vrais SMS, l'IA classe correctement au moins 2/3.

---

## ✅ Étape 8 — Calculateur de frais (Semaine 6, J1-3)

**But :** un formulaire qui renvoie un montant exact (pas une estimation IA).

Crée `frontend/src/app/fees/fees.component.{ts,html,css,spec.ts}`.

Logique : un fichier `frontend/src/app/fees/fees-data.ts` avec les grilles **en dur** (calculées depuis tes fiches Markdown) :

```ts
export const ORANGE_MONEY_TRANSFER = [
  { min: 500,    max: 5000,   fee: 50 },
  { min: 5001,   max: 25000,  fee: 200 },
  // ...
];
```

Le composant prend : opérateur + type d'opération + montant → applique la grille → affiche.

> ⚠️ **Pourquoi pas de l'IA ici ?** Parce que pour des chiffres exacts, on ne fait JAMAIS confiance à un LLM. On code la règle.

> 🤖 **Demande à l'IA :** « Génère-moi un test unitaire Jasmine qui vérifie que 10 000 FCFA Orange Money transfert = 200 FCFA de frais. »

---

## ✅ Étape 9 — Rapports CSV agents (Semaine 6, J4-5)

**But :** un agent uploade son relevé, l'IA résume.

1. Réutilise `document-service` pour l'upload.
2. Parse le CSV côté Java (librairie `opencsv` déjà classique).
3. Calcule : total volume, total commissions, top 5 jours, anomalies.
4. Envoie le résumé chiffré au LLM avec ce prompt :

```text
Voici les statistiques d'activité d'un agent Mobile Money sur le mois.
Rédige un rapport synthétique de 10 lignes en français, ton professionnel,
avec recommandations concrètes pour augmenter les commissions.
Statistiques : <JSON>
```

---

## ✅ Étape 10 — Branding « MoMo Assistant » (Semaine 7)

**But :** que ça ne ressemble plus à ai-platform.

### À changer

- `frontend/src/app/core/sidebar/sidebar.component.html` → titre « MoMo Assistant »
- Logo : remplace l'icône, garde la charte **noir / blanc / orange** (la palette Orange CI s'y prête parfaitement)
- `frontend/src/index.html` → `<title>` + `<meta description>`
- Page d'accueil du chat : message d'intro adapté
- README de ton repo : capture d'écran + description

> 🤖 **Demande à l'IA :** « Propose-moi 5 baselines (slogans) courts pour MoMo Assistant. »

---

## ✅ Étape 11 — Tests et qualité (Semaine 8, J1-3)

### Checklist obligatoire

- [ ] `mvn test` passe sur tous les services Java
- [ ] `cd frontend && npm test -- --watch=false` passe
- [ ] `docker compose up -d --build` démarre tout sans erreur
- [ ] Tu peux te connecter avec `admin.demo`
- [ ] Tu poses 10 questions Mobile Money → réponses correctes et sourcées
- [ ] Tu testes 5 SMS frauduleux → bien classés
- [ ] Tu calcules 5 montants de frais → exacts au franc près
- [ ] Aucune emoji dans l'UI (seulement Bootstrap Icons)
- [ ] Aucune couleur hors **noir / blanc / orange**

---

## ✅ Étape 12 — Démo et documentation (Semaine 8, J4-5)

### À produire

1. **`README.md`** de ton repo `momo-assistant` : description, captures, comment lancer
2. **Vidéo de démo** de 5 min (Loom / QuickTime) : tu présentes chaque module
3. **Slides** (10 max) : contexte, archi, démo, limites, perspectives
4. **Présentation orale** de 20 min à ton tuteur

---

## 🆘 Que faire quand je suis bloqué ?

### Workflow de déblocage (à appliquer DANS L'ORDRE)

1. **Relis le message d'erreur en entier** (souvent la solution est dedans)
2. **Cherche l'erreur sur Google** (copie-colle entre guillemets)
3. **Demande à MoMo Assistant lui-même** ou à ChatGPT (avec le contexte complet)
4. **Demande à un collègue technique** (5 min max, par écrit)
5. **Demande à ton tuteur** (en stand-up quotidien)

### Template de question

```
🎯 Objectif : <ce que je veux>
🔧 Contexte : <fichier que je modifie, commande lancée>
❌ Problème : <message d'erreur complet>
🤔 J'ai déjà essayé : <liste>
❓ Ma question : <précise>
```

---

## 📚 Lectures utiles (15 min/jour)

- **Git** : [https://learngitbranching.js.org](https://learngitbranching.js.org) (interactif, ludique)
- **Docker en 1 heure** : YouTube « Docker pour les débutants »
- **Angular** : [https://angular.dev/tutorials](https://angular.dev/tutorials)
- **Spring Boot** : [https://spring.io/quickstart](https://spring.io/quickstart)
- **RAG expliqué simplement** : YouTube « What is Retrieval Augmented Generation »

---

## 🏁 Conclusion

Ne cherche pas à être parfait. Cherche à **livrer quelque chose qui fonctionne**, même imparfait. Tu pourras toujours améliorer ensuite.

À chaque doute, retourne aux **Règles d'or** en haut de ce document.

Bon courage 🚀

— *Ton tuteur*

