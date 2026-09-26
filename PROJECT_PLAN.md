# API Security Gateway with ML-Based Anomaly Detection
## Agent Development Master Plan

> **Project type:** B.Tech Mini Project  
> **Primary objective:** Build an API Security Gateway that intercepts API/HTTP requests, extracts security-relevant features, uses a machine-learning anomaly detector to assess requests, and allows or blocks traffic before it reaches the target backend.
>
> **Development environment:** Antigravity / AI coding agent initially. The project must remain portable and runnable from a normal terminal/VS Code/IntelliJ later.
>
> **Important:** The agent must implement the project incrementally. Do NOT generate the entire system in one pass. Every phase must be implemented, run, tested, and verified before moving to the next phase.

---

# 0. PROJECT IDENTITY

## Final project name

**API Security Gateway with Machine Learning-Based Anomaly Detection**

Use **API Security Gateway** as the main system name.

Do not call the whole project "AI Security Gateway". AI/ML is one component of the API Security Gateway.

## Core problem

A normal API architecture looks like:

```text
Client → Backend API
```

The proposed architecture is:

```text
Client
  ↓
API Security Gateway
  ↓
Request Analysis
  ↓
ML Anomaly Detection
  ↓
┌───────────────┬────────────────┐
│ NORMAL        │ ANOMALOUS      │
│               │                │
▼               ▼                ▼
Forward       Block            Log
  ↓
Backend API
```

The gateway should become the security boundary between clients and the target API.

---

# 0.5 NOVELTY & RESEARCH CONTRIBUTION (READ FIRST)

## Why this matters

A Spring Boot gateway + Isolation Forest on HTTP requests, by itself, is not a new idea — many papers already do this on CSIC 2010. Building only that produces a working engineering project but **not a publishable result**. This section defines the specific contribution that makes the paper worth writing, and every later phase must serve it.

## Framing: don't assert the gap — investigate it

Do not open the paper by claiming "existing work is evaluated only on static, non-adversarial data, therefore our evaluation is novel." That's a hypothesis, not a fact yet, and a reviewer's first move will be "show me the papers proving that." Phrase Chapter 2 (Literature Survey) as an investigation: *"We examine whether existing HTTP/API anomaly-detection work evaluates robustness against obfuscated or adversarial attack variants."* Only after the literature review is actually done do you get to state, as a finding, whether that gap exists and how your evaluation addresses it. If the review turns up papers that already do this, that's fine — pivot the novelty claim to what's still missing (e.g., comparison across learning paradigms + LLM-paraphrased variants specifically, rather than "adversarial testing" in general).

## The research questions (RQ) — framed by learning paradigm, not "best model"

The paper is not answering "which ML model is best" — that's a weak framing. It's answering **how different learning paradigms perform** on the same task:

| Approach | Learning paradigm | Sees during training | Role in the paper |
|---|---|---|---|
| Rule-based signatures (regex/pattern) | None (hand-coded) | N/A | Baseline — "what does zero ML get you" |
| Isolation Forest | Unsupervised | Normal traffic only | Primary anomaly detector |
| One-Class SVM | Unsupervised (one-class) | Normal traffic only | Comparison |
| LOF | Unsupervised | Normal traffic only | Comparison |
| Random Forest | Supervised | Normal + attack + labels | Reference ceiling (uses more information than the others — not a fair "winner," a sanity check on how much label information buys you) |

This paradigm difference is the experimental protocol, and it must be stated explicitly in the methodology chapter: the three unsupervised models only ever see normal traffic at training time and are tested on a held-out mix of normal + attack; Random Forest sees labelled normal + attack at training time. Comparing their test-time metrics is valid; comparing their training data is not "unfair" as long as this is disclosed — it's the whole point of the comparison.

- **RQ1 (Learning paradigm comparison):** How do unsupervised anomaly detectors (Isolation Forest, One-Class SVM, LOF), a rule-based signature baseline, and a supervised reference (Random Forest) compare on precision/recall/F1 for HTTP attack detection, given that each uses a different amount of information at training time?
- **RQ2 (Robustness under obfuscation):** How much does each approach's Attack Detection Rate (ADR) degrade when attacks are obfuscated (encoding, whitespace/comment insertion, case variation, LLM-paraphrased payloads) versus clean attacks from the same taxonomy? Define:

  ```text
  ADR = detected malicious requests / total malicious requests
  Degradation = ADR(clean attacks) − ADR(obfuscated attacks)
  ```

  Measured per model, per attack category. This is the strongest and most concrete result in the paper — a rule-based baseline is expected to degrade sharply on obfuscated payloads (its patterns are literal), and the interesting question is whether the ML approaches degrade less.
- **RQ3 (Explainability):** For a request an anomaly detector flags, which features drove that decision (global: permutation importance; per-request: SHAP), and does that match human security intuition?

## What this adds on top of the original plan

| Addition | Where it plugs in | Cost |
|---|---|---|
| Rule-based signature baseline (SQLi/XSS/path-traversal/command-injection patterns) | Phase 14 | Medium — this is now part of RQ1/RQ2, not optional polish |
| Train One-Class SVM, LOF, and Random Forest alongside Isolation Forest, with the training-data protocol above made explicit | Phase 10 | Low — same feature matrix, different `sklearn` estimator |
| k-fold cross-validation instead of a single split, report mean ± std | Phase 11 | Low |
| Dataset leakage checks: duplicate/near-duplicate requests, attack-template overlap between train/test | Phase 8 | Low |
| Explainability: global permutation importance + per-request SHAP | Phase 11B | Medium |
| Attack taxonomy with per-category ADR | Phase 7 + Phase 20 | Low |
| Adversarial evasion test set as an **offline research dataset**, never a runtime gateway dependency | Phase 20 (Experiment E) | Medium — this is the novelty engine of the paper |

## Core / Research / Optional — use this to decide where your remaining time goes

If time runs short, cut from the bottom, never from the middle:

- **CORE (must work for any grade at all):** gateway, backend, proxy, dataset, preprocessing, feature engineering, Isolation Forest, FastAPI ML service, gateway↔ML integration, allow/block decision, basic evaluation.
- **RESEARCH (this is what the paper is actually about — do not sacrifice this for polish):** rule-based baseline, One-Class SVM, LOF, Random Forest reference, 5-fold cross-validation, dataset leakage checks, attack taxonomy, obfuscation/evasion experiment, explainability.
- **OPTIONAL (portfolio value, cut first under time pressure):** MongoDB, React dashboard, Docker, rate limiting, circuit breaker, advanced auth.

If you have two weeks left before submission, it should never be the case that the dashboard works but the model-comparison table doesn't exist.

## Research methodology pipeline (separate from the build-phase list below)

The numbered phases below are the *build* order. Independently, keep this *scientific* pipeline in mind when writing the paper — it's the structure Chapter 2 onward should follow, and it doesn't map one-to-one onto build phases:

```text
Research Problem → Research Questions → Literature Review → Dataset Selection
→ Data Preparation (incl. leakage checks) → Feature Engineering → Baseline (rule-based)
→ ML Models (paradigm-labelled) → Experimental Protocol → Cross-Validated Evaluation
→ Robustness/Obfuscation Testing → Explainability → Results → Discussion
→ Limitations → Conclusion
```

## Positioning statement for the paper's introduction (adjust after the literature review confirms it)

Draft language, to be revised once Chapter 2 is actually written: *"We compare anomaly-detection approaches spanning three learning paradigms (rule-based, unsupervised, supervised-reference) under an identical, leakage-checked feature pipeline, and evaluate robustness against obfuscated and LLM-paraphrased attack variants — an evaluation we found to be under-represented in the HTTP/API anomaly-detection literature we reviewed."* Don't finalize this until the literature review backs the "under-represented" claim.

## Dataset selection: recency is one criterion among several, not the deciding one

Don't select a dataset mainly because it's newer than CSIC 2010. Newer isn't automatically better. Evaluate candidates on: recency, HTTP/API relevance, attack diversity, normal-traffic realism, label quality, dataset size, collection methodology, license, and absence of obvious leakage. If the best-scoring dataset turns out to be from 2023 or 2024 rather than 2025, that's a fine, defensible choice — document the scoring, not just the year.

## Fallback if no dataset scores well enough

If nothing scores well within a fixed time box (recommend: 3 days), fall back to:

- CSIC 2010 as the primary labelled dataset (well-documented, defensible), **plus**
- Self-generated traffic: deploy a deliberately vulnerable local app (OWASP Juice Shop or DVWA), generate normal traffic manually/scripted, and generate labelled attack traffic using a tool such as OWASP ZAP or sqlmap against your own local instance only.

This combination is scientifically weaker than a strong modern dataset but is fully reproducible and still supports RQ1–RQ3. Document this decision explicitly in `dataset/README.md` either way — do not silently downgrade.

---

# 1. FINAL TARGET ARCHITECTURE

The final system should approximately follow this architecture:

```text
                         CLIENT
                    Browser / Postman / cURL
                              │
                              │ HTTP API Request
                              ▼
                  ┌─────────────────────────┐
                  │   API SECURITY GATEWAY  │
                  │        Spring Boot      │
                  │                         │
                  │ • Request interception  │
                  │ • Validation             │
                  │ • Raw metadata capture   │
                  │ • Security policy        │
                  └────────────┬────────────┘
                               │
                               │ Raw HTTP request metadata
                               ▼
                  ┌─────────────────────────┐
                  │      ML SERVICE         │
                  │     Python + FastAPI     │
                  │                          │
                  │ • Feature extraction     │
                  │ • Preprocessing          │
                  │ • Isolation Forest /     │
                  │   comparison models      │
                  └────────────┬────────────┘
                               │
                               │ Prediction
                               ▼
                    ┌────────────────────┐
                    │ Decision Engine    │
                    └─────────┬──────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
                 NORMAL              ANOMALY
                    │                   │
                    ▼                   ▼
             Forward Request          BLOCK
                    │                 (403)
                    ▼
             Target Backend API
                    │
                    ▼
                 Response

                  Security Events
                         │
                         ▼
                  MongoDB / Logs
                         │
                         ▼
                    Dashboard
```

**Architectural rule:** feature extraction happens in exactly one place — the Python ML service (`ml-service/app/feature_extractor.py`). The gateway only captures and forwards raw request metadata (method, path, query, headers, body, timestamp). The same reason applies as before: whatever code turns a request into numbers must be identical during training and live inference, and that's only guaranteed if it lives in one codebase rather than being duplicated in Java and Python.

---

# 2. IMPORTANT DEVELOPMENT RULES FOR THE AGENT

## Rule 1 — Work phase by phase

Never implement the entire project at once.

Required order:

1. Environment verification
2. Repository/project structure
3. Spring Boot gateway skeleton
4. Target backend
5. Gateway-to-backend proxy
6. Dataset acquisition and inspection
7. Dataset preprocessing
8. Feature engineering
9. Isolation Forest model
10. ML API
11. Gateway–ML integration
12. Security decision/blocking
13. Logging/database
14. Dashboard
15. Testing/evaluation
16. Documentation
17. Final cleanup

## Rule 2 — Keep the system runnable after every phase

After each phase:

- run the application
- run relevant tests
- inspect errors
- fix them
- document what changed
- only then continue

## Rule 3 — Do not invent dataset properties

Before writing dataset-processing code:

- verify the official dataset source
- inspect the actual files
- identify columns/fields
- identify labels
- determine how HTTP requests are represented
- determine whether WAF-generated labels/rules could cause data leakage

Never assume that the new dataset has the same format as CSIC 2010.

## Rule 4 — Avoid data leakage

If the dataset contains fields such as:

```text
WAF rule triggered
attack label
ModSecurity decision
rule ID
```

do NOT automatically use those fields as ML input features.

Those fields may reveal the answer to the model.

Use them as labels/evaluation information where appropriate.

## Rule 5 — Do not claim results before experiments

Never write:

```text
Accuracy = 95%
F1 = 0.94
```

unless the value was actually obtained from a reproducible experiment.

Use placeholders such as:

```text
TBD - measured after model training
```

until the experiment is completed.

## Rule 6 — Security testing must remain controlled

Use only:

- local applications
- intentionally vulnerable/test endpoints
- provided datasets
- harmless representative payloads
- systems for which we have authorization

Do not test the gateway against third-party systems.

---

# 3. REPOSITORY STRUCTURE

The final repository should look approximately like:

```text
api-security-gateway/
│
├── gateway/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
├── ml-service/
│   ├── app/
│   │   ├── main.py
│   │   ├── schemas.py
│   │   ├── predictor.py
│   │   └── feature_extractor.py
│   ├── model/
│   ├── tests/
│   ├── requirements.txt
│   └── README.md
│
├── dataset/
│   ├── raw/
│   ├── processed/
│   ├── scripts/
│   │   └── generate_evasion_variants.py
│   └── README.md
│
├── frontend/
│   └── ...
│
├── docs/
│   ├── architecture/
│   ├── literature/
│   ├── experiments/
│   │   ├── model-evaluation.md
│   │   ├── explainability.md
│   │   └── adversarial-evasion.md
│   └── screenshots/
│
├── docker-compose.yml        # optional later
├── .gitignore
├── README.md
└── PROJECT_PLAN.md
```

> **FOR KRISHNA:** This separation is intentional. `gateway` is the Java security layer, `backend` is the application being protected, `ml-service` is the Python model service, and `frontend` is only the monitoring UI. Keeping them separate makes debugging and later deployment much easier.

---

# 4. PHASE 1 — ENVIRONMENT VERIFICATION

## Objective

Verify that all required development tools exist before generating code.

## Required tools

- Java JDK 21
- Maven
- Python 3.x
- pip
- Git
- Node.js + npm (needed later for React)
- VS Code or another editor
- MongoDB (can be installed/configured later)
- cURL
- Postman optional

## Verification commands

Run:

```bash
java -version
mvn -version
python3 --version
pip3 --version
git --version
node --version
npm --version
curl --version
```

## Acceptance criteria

All required commands work.

If something is missing, install it before continuing.

> **FOR KRISHNA:** Do not install duplicate versions unnecessarily. First check what is already installed. We want Java 21 and a compatible Maven installation for the Spring Boot project.

---

# 5. PHASE 2 — INITIALIZE PROJECT

## Objective

Create the repository and basic folder structure.

Create:

```text
api-security-gateway/
```

Initialize Git:

```bash
git init
```

Create:

```text
gateway/
backend/
ml-service/
dataset/raw/
dataset/processed/
dataset/scripts/
frontend/
docs/
```

Create a root `.gitignore`.

The `.gitignore` must exclude:

```text
target/
.idea/
.vscode/
__pycache__/
*.pyc
.venv/
node_modules/
.env
dataset/raw/
model binaries if they are too large
```

Do not commit secrets.

## Acceptance criteria

```bash
git status
```

works and the project has the expected structure.

---

# 6. PHASE 3 — SPRING BOOT GATEWAY

## Objective

Create the first working API Security Gateway application.

## Technology

- Java 21
- Spring Boot 3.x
- Maven
- Spring Web
- Spring Boot Actuator

## Create project

Recommended metadata:

```text
Group: com.krishna
Artifact: gateway
Package: com.krishna.gateway
Packaging: Jar
Java: 21
```

## Initial dependencies

- Spring Web
- Spring Boot Actuator

Do not add unnecessary dependencies yet.

## First endpoint

Create:

```http
GET /api/health
```

Expected response:

```json
{
  "status": "UP",
  "service": "API Security Gateway"
}
```

## Configuration

Initially:

```properties
server.port=8080
```

## Acceptance test

```bash
curl http://localhost:8080/api/health
```

Expected HTTP 200.

> **FOR KRISHNA:** This endpoint proves that the Spring Boot gateway is alive. It is not the security logic yet. We build the smallest working component first so later failures are easier to isolate.

---

# 7. PHASE 4 — TARGET BACKEND API

## Objective

Create a local API application that the gateway will protect.

Create a second Spring Boot application:

```text
Group: com.krishna
Artifact: backend
Package: com.krishna.backend
Java: 21
```

Dependency:

```text
Spring Web
```

Configure:

```properties
server.port=3000
```

## Endpoints

Create:

```http
GET /api/backend/health
GET /api/products
GET /api/users
```

Example:

```json
[
  "Laptop",
  "Keyboard",
  "Mouse",
  "Monitor"
]
```

## Acceptance tests

```bash
curl http://localhost:3000/api/backend/health
curl http://localhost:3000/api/products
```

Both should work.

> **FOR KRISHNA:** This backend is deliberately simple. It represents the real application/API that our gateway is supposed to protect. We should not make the target application complex because the security gateway is the project focus.

---

# 8. PHASE 5 — BASIC GATEWAY → BACKEND PROXY

## Objective

Make the gateway forward legitimate requests to the backend.

Desired behavior:

```text
Client
  ↓
GET http://localhost:8080/api/products
  ↓
Gateway
  ↓
GET http://localhost:3000/api/products
  ↓
Backend
```

The client must NOT need to call port 3000 directly.

## Requirements

The gateway should:

1. receive the incoming request
2. determine the target backend route
3. forward method, path and relevant request information
4. receive backend response
5. return backend response to client

## Important

Do not add ML yet.

First prove:

```text
Gateway → Backend
```

works reliably.

## Acceptance criteria

This should work:

```bash
curl http://localhost:8080/api/products
```

and return the backend's response.

Direct backend access:

```bash
curl http://localhost:3000/api/products
```

should still work during development, but the intended architecture is that clients use the gateway.

> **FOR KRISHNA:** This phase establishes the gateway as a real reverse-proxy layer. If this does not work, ML integration will only make debugging harder.

---

# 9. PHASE 6 — REQUEST INTERCEPTION / GATEWAY PIPELINE

## Objective

Introduce a clear security processing pipeline before forwarding requests.

Desired internal flow:

```text
Incoming Request
      ↓
Request Interceptor
      ↓
Request Metadata Extraction
      ↓
Security Analysis
      ↓
Decision
      ↓
ALLOW / BLOCK
      ↓
Forward if allowed
```

Initially the decision can always be:

```text
ALLOW
```

because ML is not connected yet.

## Capture metadata

At minimum:

- timestamp
- HTTP method
- request path
- query string
- content type
- content length
- selected headers
- source IP where available

Do not log sensitive values unnecessarily.

> **FOR KRISHNA:** This is where the gateway starts becoming a security gateway rather than just a proxy. We are creating a clean insertion point for the ML detector.

---

# 10. PHASE 7 — DATASET FINALIZATION

## Objective

Use a recent HTTP/WAF dataset rather than making CSIC 2010 the primary training dataset.

## Preferred direction

Use a **2025 ModSecurity/WAF HTTP attack dataset** as the primary candidate, subject to verification of its actual downloadable data and schema.

Possible supplementary data:

- another recent labelled WAF dataset such as ModSec-WP
- Biblio-US17 as a large real HTTP-traffic dataset
- CSIC 2010 only as a historical baseline if useful

## Important decision rule

Do NOT combine datasets automatically.

First inspect:

```text
dataset size
file format
fields
HTTP request representation
normal/malicious labels
attack classes
duplicate records
missing values
class balance
data collection method
license
```

## Dataset documentation

Create:

```text
dataset/README.md
```

Document:

- official source
- publication/source paper
- year
- license
- number of records
- fields
- labels
- attack categories
- why it is relevant
- limitations

## Attack taxonomy (required for RQ2/RQ3 later)

Before preprocessing, define and document a fixed set of attack categories the dataset's malicious samples will be mapped to, e.g.:

```text
SQL injection
Cross-site scripting (XSS)
Path traversal
Command injection
SSRF / other
```

Record the count of samples per category in `dataset/README.md`. If the chosen dataset doesn't label attack sub-type, do a best-effort keyword-based tagging and document it as approximate — this taxonomy is what lets Phase 20's per-category detection-rate results exist later.

## Acceptance criteria

A small sample can be loaded successfully with Python.

> **FOR KRISHNA:** The reason for preferring a recent dataset is not simply that “2025 is newer”. We want data that is more relevant to current WAF/API traffic and, ideally, comes from realistic security/WAF environments. The exact dataset choice must be justified by its contents.

---

# 11. PHASE 8 — DATA PREPROCESSING

## Objective

Convert raw dataset records into a clean representation of HTTP requests.

Create:

```text
dataset/scripts/inspect_dataset.py
dataset/scripts/preprocess_dataset.py
```

## Tasks

1. Load raw data
2. Inspect schema
3. Remove corrupted records
4. Handle missing values
5. Remove duplicates where appropriate
6. Normalize HTTP methods
7. Extract URL/path/query/body
8. Normalize encoding carefully
9. Preserve labels separately
10. Remove leakage-prone fields from model inputs
11. Check for train/test leakage (below) — do this **before** the split is finalized
12. Save processed data

## Leakage checks (required, separate from field-level leakage in Rule 4)

Field-level leakage (excluding a "WAF rule triggered" column from the features) is not the only kind. Also check, and document counts for:

```text
Exact duplicate requests across the dataset
Near-duplicate requests (same payload, trivially different parameter values)
Same attack template appearing in both train and test split
Attack-family overlap that could let a model memorize a template rather than generalize
```

If duplicates/near-duplicates are found, deduplicate **before** the train/test split, not after — a duplicate split across train and test silently inflates every metric in Phase 11. Record what was found and removed in `dataset/README.md`; an unusually high metric with no leakage discussion is exactly what an examiner will probe first.

Expected output:

```text
dataset/processed/processed_requests.csv
```

## Example conceptual schema

```text
request_id
method
path
query
body
headers
label
attack_type
```

The actual schema must be based on the selected dataset.

> **FOR KRISHNA:** Do not copy this schema blindly. Different datasets store requests differently. The preprocessing script must adapt to the actual selected dataset.

---

# 12. PHASE 9 — FEATURE ENGINEERING

## Objective

Convert HTTP requests into numerical features that Isolation Forest can process.

Potential request-level features:

### Size features

- total request length
- URL length
- path length
- query length
- body length

### Character features

- alphabet count
- digit count
- whitespace count
- special-character count
- quote count
- angle-bracket count
- semicolon count
- slash count

### Structural features

- number of query parameters
- number of path segments
- HTTP method
- content type

### Security-oriented lexical features

- suspicious keyword count
- encoded-character count
- repeated-symbol count
- script-related token count
- SQL-like token count

### Statistical feature

- character entropy

## Important

Do not create hundreds of arbitrary features without justification.

Start with a compact, interpretable feature set and expand only if experiments show a need.

## Feature extractor

Create:

```text
ml-service/app/feature_extractor.py
```

or a shared preprocessing implementation if architecture requires it.

The same feature extraction logic used during training must be available during live prediction.

> **FOR KRISHNA:** This is one of the most important parts of the project. The ML model cannot understand raw HTTP text directly. We transform each request into a numerical feature vector.

---

# 13. PHASE 10 — TRAIN ISOLATION FOREST + COMPARISON MODELS

## Objective

Train an anomaly-detection model — Isolation Forest is the primary/production model, but for RQ1 it must be trained and evaluated alongside comparison models on the identical feature matrix, not in isolation:

```text
Primary:      Isolation Forest (unsupervised)
Comparison:   One-Class SVM (unsupervised)
Comparison:   Local Outlier Factor / LOF (unsupervised)
Reference:    Random Forest (supervised, uses labels — only valid where labels exist)
```

The supervised Random Forest is a **reference ceiling**, not a candidate for deployment — its purpose is to show how much an unsupervised approach gives up (or doesn't) by not using labels. This comparison table is one of the most citable results in the paper; do not skip it to save time.

## Training-data protocol (state this explicitly in the methodology chapter)

Each model type sees different information at training time — that's the point of the comparison (RQ1), and it must be disclosed, not glossed over:

```text
Isolation Forest, One-Class SVM, LOF:
    train on NORMAL traffic only
    test on held-out NORMAL + ATTACK traffic

Random Forest:
    train on labelled NORMAL + ATTACK traffic
    test on the same held-out set as above
```

Comparing test-time metrics across these is valid science. Comparing what each saw during training and calling it "unfair" misses the point — the experiment is specifically about how much having labels helps.

Technology:

```text
Python
Pandas
NumPy
Scikit-learn
Joblib
```

## Why Isolation Forest?

The problem is primarily anomaly detection:

```text
Normal API traffic
        ↓
Learn normal structure
        ↓
New request
        ↓
How unusual is it?
```

Isolation Forest is appropriate because anomalous observations tend to be isolated more easily through random partitions.

## Important modeling decision

If labelled data exists:

- use labels to evaluate the model
- do not automatically feed the attack label into the feature vector
- avoid leakage

## Training pipeline

```text
Processed dataset
      ↓
Feature extraction
      ↓
Training feature matrix
      ↓
Train Isolation Forest
      ↓
Save model
```

Save model using a controlled format such as:

```text
ml-service/model/isolation_forest.joblib
```

## Initial model parameters

Start with reasonable defaults and document them.

Potential parameters:

```text
n_estimators
contamination
max_samples
random_state
```

Do not optimize blindly.

---

# 14. PHASE 11 — MODEL EVALUATION

## Objective

Measure whether the detector actually works — and whether it's actually the best choice among the models trained in Phase 10.

## Cross-validation requirement

A single train/test split is not sufficient evidence for a paper. Use k-fold cross-validation (k=5 is a reasonable default) and report **mean ± standard deviation** for every metric below, for every model in Phase 10. A single-split number with no variance reported will draw an immediate reviewer/examiner objection.

If labelled test data is available, calculate, per model, per fold:

- Precision
- Recall
- F1-score
- Confusion matrix
- False positives
- False negatives

Then produce one comparison table across Isolation Forest / One-Class SVM / LOF / Random Forest — this table directly answers RQ1.

Also inspect:

- class distribution
- anomaly score distribution
- threshold/contamination sensitivity

## Important security interpretation

False positives matter because:

```text
Normal API request
      ↓
Incorrectly blocked
      ↓
Legitimate user affected
```

False negatives matter because:

```text
Malicious request
      ↓
Incorrectly allowed
      ↓
Security risk
```

Do not optimize for accuracy alone.

## Experiment documentation

Create:

```text
docs/experiments/model-evaluation.md
```

Record:

- dataset version
- feature version
- train/test split
- model parameters
- random seed
- metrics
- observations

> **FOR KRISHNA:** If the final model has weak results, do not hide it. Document the result and improve the feature engineering/model configuration. That is much stronger academically than inventing a high score.

---

# 14.5. PHASE 11B — EXPLAINABILITY (ANSWERS RQ3)

## Objective

Make the anomaly decision auditable instead of a black-box score.

## Approach

For the primary model (Isolation Forest), compute per-prediction feature contribution using one of:

```text
Permutation feature importance (global, simplest, no extra dependency)
SHAP values (per-instance, more work, stronger paper content)
```

Start with permutation importance since it needs no new library. Add SHAP only if time allows — it produces a much stronger figure for the paper (per-request bar chart of which features pushed the score toward "anomalous").

## Deliverable

- `docs/experiments/explainability.md` — global feature importance ranking, plus 3–5 worked examples: one flagged normal request, one correctly flagged attack, and (if any exist) one false positive/negative with an explanation of which features misled the model.
- Sanity-check the ranking against security intuition: features like special-character count, SQL-like token count, and entropy should rank highly for attack detection. If they don't, that itself is a documented finding, not a bug to hide.

> **FOR KRISHNA:** This phase is what turns "the model flags anomalies" into "the model flags anomalies *and we can explain why*", which examiners and reviewers specifically look for in security-ML work.

---

# 15. PHASE 12 — BUILD PYTHON ML API

## Objective

Expose the trained model through a local REST API.

Technology:

- FastAPI
- Uvicorn
- Pydantic
- Scikit-learn
- Joblib

Directory:

```text
ml-service/
├── app/
│   ├── main.py
│   ├── schemas.py
│   ├── predictor.py
│   └── feature_extractor.py
├── model/
├── tests/
└── requirements.txt
```

## Endpoint

```http
POST /predict
```

Example conceptual request:

```json
{
  "method": "GET",
  "path": "/products",
  "query": "id=10",
  "body": ""
}
```

Example conceptual response:

```json
{
  "prediction": "NORMAL",
  "anomaly_score": 0.12
}
```

The actual score direction/interpretation must be documented correctly based on the implementation. Do not assume that a larger raw Isolation Forest score always means “more malicious”.

## Health endpoint

```http
GET /health
```

Expected:

```json
{
  "status": "UP"
}
```

## Acceptance criteria

```bash
curl http://localhost:8000/health
```

works.

`/predict` returns a valid structured response.

---

# 16. PHASE 13 — GATEWAY ↔ ML SERVICE INTEGRATION

## Objective

Connect Spring Boot to FastAPI.

Final request path:

```text
Client
  ↓
Spring Boot Gateway :8080
  ↓
Feature extraction
  ↓
POST /predict
  ↓
FastAPI :8000
  ↓
Isolation Forest
  ↓
Prediction
  ↓
Gateway
```

## Gateway behavior

For each protected request:

1. capture request
2. build ML input
3. call ML service
4. receive prediction
5. apply security policy
6. allow or block

## Failure behavior

Define what happens if ML service is unavailable.

For a mini-project, implement a clearly documented policy such as:

```text
ML unavailable → fail closed OR controlled fallback
```

The chosen policy must be explicit and tested.

> **FOR KRISHNA:** This is an important design decision. A security gateway should not silently behave unpredictably when the ML service is down.

---

# 17. PHASE 14 — SECURITY DECISION ENGINE

## Objective

Separate ML prediction from enforcement logic.

Do not put all decision logic directly inside the HTTP controller.

Suggested conceptual structure:

```text
Gateway
│
├── RequestInterceptor
├── MetadataCapture        (raw fields only — no feature engineering here, see Section 1)
├── RuleBasedDetector       (signature/regex baseline — see Phase 14B below)
├── MLClient
├── SecurityDecisionService
├── ProxyService
└── SecurityLogService
```

## Decision flow

```text
Prediction
    ↓
Security Policy
    ↓
┌─────────────┬─────────────┐
│ NORMAL      │ ANOMALY     │
│             │             │
▼             ▼
ALLOW         BLOCK
              │
              ▼
             403
```

The threshold/policy must be configurable.

Example:

```properties
security.ml.enabled=true
security.anomaly.threshold=...
```

Do not hardcode experimental thresholds into source code.

---

# 17.5. PHASE 14B — RULE-BASED SIGNATURE BASELINE (RESEARCH TIER, NOT OPTIONAL)

## Objective

Give RQ1 and RQ2 something to compare the ML detectors against besides each other. A project literally named "API Security Gateway" that never compares to a signature approach invites the obvious question "how is this different from a regex WAF." This does not mean cloning ModSecurity — a small, honestly-scoped baseline is enough.

## Scope

Implement pattern/signature checks for the same attack taxonomy defined in Phase 7:

```text
SQL injection      — e.g. quote/keyword/comment patterns
XSS                — e.g. <script>, event-handler attributes, javascript: URIs
Path traversal     — e.g. ../ sequences, encoded variants
Command injection  — e.g. shell metacharacters, chained commands
```

A dozen or two well-chosen regex/signature rules is sufficient — document them in `docs/architecture/rule-baseline.md` along with their source (OWASP cheat sheets are a reasonable reference).

## Where it plugs in

- Runs as `RuleBasedDetector` in the gateway (see Phase 14's structure), independent of the ML path, so it can be tested in isolation.
- Included as a row in every RQ1 comparison table and every RQ2 obfuscation/degradation table, alongside Isolation Forest / One-Class SVM / LOF / Random Forest.
- Expected (not guaranteed) finding worth writing up either way: the rule baseline should perform reasonably on clean/known attacks and degrade sharply on obfuscated variants, since its patterns are literal. If it doesn't degrade the way expected, that's a real finding too — report what actually happens.

## Acceptance criteria

- The rule baseline runs against the same test set as the ML models (Phase 20).
- Its clean-attack and obfuscated-attack detection rates are in the same comparison tables as the ML models.

---

# 18. PHASE 15 — SECURITY LOGGING

## Objective

Record security decisions for analysis.

Log fields:

```text
timestamp
request ID
HTTP method
path
prediction
anomaly score
action
processing time
```

Avoid logging:

- passwords
- authorization tokens
- cookies
- sensitive personal data
- complete request bodies unless explicitly safe

## First implementation

Plain structured logs are acceptable.

## Later

Use MongoDB for persistent security events.

Example conceptual document:

```json
{
  "timestamp": "...",
  "method": "GET",
  "path": "/api/products",
  "prediction": "ANOMALY",
  "anomalyScore": 0.87,
  "action": "BLOCK"
}
```

---

# 19. PHASE 16 — MONGODB

## Objective

Persist security events.

Use MongoDB only after the gateway and ML pipeline work.

Create a collection such as:

```text
security_events
```

Repository/service layer:

```text
SecurityEvent
SecurityEventRepository
SecurityLogService
```

## Acceptance criteria

After a request:

```text
security event
     ↓
MongoDB
```

A dashboard/API can retrieve recent events.

---

# 20. PHASE 17 — MONITORING API

Create gateway/admin endpoints such as:

```http
GET /api/security/stats
GET /api/security/events
```

Possible statistics:

```text
total requests
allowed requests
blocked requests
anomaly rate
recent anomalies
```

Do not expose sensitive logs publicly.

These endpoints should be protected or restricted appropriately in the final design.

---

# 21. PHASE 18 — REACT DASHBOARD

## Objective

Create a simple monitoring dashboard.

Technology:

- React
- Vite
- JavaScript/TypeScript
- Chart.js or another lightweight charting library if useful

Dashboard sections:

```text
Total Requests
Allowed
Blocked
Anomaly Rate
Recent Security Events
```

Example:

```text
┌─────────────────────────────────────────────┐
│             API SECURITY GATEWAY            │
├────────────┬────────────┬───────────────────┤
│ Requests   │ Allowed    │ Blocked           │
│ 10,245     │ 9,812      │ 433               │
├────────────┴────────────┴───────────────────┤
│ Recent Security Events                      │
│                                             │
│ GET /api/products     NORMAL      ALLOWED   │
│ POST /api/login       NORMAL      ALLOWED   │
│ GET /api/search       ANOMALY     BLOCKED   │
└─────────────────────────────────────────────┘
```

These numbers are only examples. Never present example numbers as real experimental results.

---

# 22. PHASE 19 — END-TO-END TESTING

## Objective

Prove the complete system works.

## Test 1 — Gateway health

```bash
curl http://localhost:8080/api/health
```

Expected: 200.

## Test 2 — Backend health

```bash
curl http://localhost:3000/api/backend/health
```

Expected: 200.

## Test 3 — Gateway forwarding

```bash
curl http://localhost:8080/api/products
```

Expected: backend response.

## Test 4 — Normal request

Send a normal API request.

Expected:

```text
Gateway
 ↓
ML
 ↓
NORMAL
 ↓
Backend
 ↓
200
```

## Test 5 — Representative suspicious request

Use controlled local test payloads representing common web-attack patterns.

Expected:

```text
Gateway
 ↓
ML
 ↓
ANOMALY
 ↓
BLOCK
 ↓
403
```

The actual result must be measured. Do not assume every attack string will be detected.

## Test 6 — ML service failure

Stop FastAPI and send a request.

Verify the documented fallback/fail-closed policy.

## Test 7 — Backend failure

Stop backend.

Verify gateway returns a controlled error rather than crashing.

## Test 8 — Performance

Measure approximately:

- gateway latency without ML
- gateway latency with ML
- ML prediction latency
- total request latency

---

# 23. PHASE 20 — MODEL EXPERIMENTS

Run controlled experiments.

## Experiment A — Baseline

Basic feature set + Isolation Forest.

## Experiment B — Feature improvement

Add justified features.

Compare metrics.

## Experiment C — Parameter tuning

Test a small set of:

```text
n_estimators
contamination
max_samples
```

## Experiment D — Dataset comparison

If multiple datasets are used, test:

```text
Dataset A
Dataset B
Combined data
```

Do not combine data simply to obtain better metrics.

Document why a configuration was selected.

## Experiment E — Adversarial evasion testing (answers RQ2 — this is the paper's novelty engine)

## Objective

Test whether each detector from Phase 10, plus the rule-based baseline (Phase 14B), still catches attacks when those attacks are deliberately altered to evade pattern-based detection, and compare the degradation across all of them.

## This is an offline research step, not a gateway feature

Build this as a standalone offline pipeline (dataset script → variant generator → saved evasion test set → run each model against it → record results). Do **not** wire an LLM into the live gateway request path — the LLM is a one-time data-generation tool for building the evaluation set, never a runtime dependency:

```text
Offline: attack taxonomy → transformation rules / LLM paraphrasing → saved evasion dataset (CSV/JSON)
Offline: run each trained model + rule baseline against the saved evasion dataset
Offline: compute ADR and degradation, write results to docs/
```

The gateway itself never calls an LLM at request time.

## Building the evasion test set

Starting from the attack taxonomy (Phase 7), construct obfuscated variants of known attack payloads using techniques such as:

```text
Encoding tricks (URL-encoding, double-encoding, case variation)
Whitespace/comment insertion inside SQL/script payloads
Equivalent-but-differently-worded payloads (e.g. LLM-paraphrased XSS/SQLi strings)
Parameter splitting / reordering
```

An LLM (used only to generate payload *text* offline, never executed against any live system other than your own local authorized test backend) is a reasonable way to generate paraphrased variants at scale. Keep every generated payload local, and only run it against your own local backend/gateway from Phase 3–5 — never against a third-party system, per Rule 6.

## Required measurements

For each model from Phase 10 **and** the rule-based baseline from Phase 14B, per attack category from the taxonomy:

```text
ADR (Attack Detection Rate) = detected malicious requests / total malicious requests
ADR on clean attack samples
ADR on obfuscated/evasive variants
Degradation = ADR(clean) − ADR(obfuscated)
```

Report this as one table across all five approaches (rule-based, Isolation Forest, One-Class SVM, LOF, Random Forest) and a short discussion: does the rule-based baseline degrade more sharply than the ML approaches, as expected for literal pattern matching — or does something more interesting happen? This is the concrete, defensible novelty claim for the paper — state it only with the measured numbers, not before.

## Acceptance criteria

- `docs/experiments/adversarial-evasion.md` documents the evasion generation method, sample counts per category, and the full degradation table (all five approaches).
- All evasion testing was run only against the project's own local backend/gateway, offline.

---

# 24. PHASE 21 — SECURITY AND ENGINEERING IMPROVEMENTS

After the MVP works, consider:

### Rate limiting

Protect against excessive requests.

### API key/authentication

Optional depending on project scope.

### Request size limits

Prevent extremely large requests.

### Timeout handling

Gateway → ML timeout.

### Circuit breaker

Avoid repeated ML-service failures.

### Configuration management

Use environment variables for:

```text
backend URL
ML service URL
MongoDB URI
threshold
ports
```

Never hardcode secrets.

---

# 25. PHASE 22 — DOCKERIZATION (OPTIONAL)

Only after the local version works.

Potential services:

```text
gateway
backend
ml-service
mongodb
frontend
```

Use:

```text
docker-compose.yml
```

Do not introduce Docker early if it makes debugging harder.

---

# 26. PHASE 23 — DOCUMENTATION

Maintain documentation throughout development.

Required documents:

```text
README.md
docs/architecture.md
docs/dataset.md
docs/methodology.md
docs/experiments/model-evaluation.md
docs/testing.md
```

README should include:

- project overview
- architecture
- prerequisites
- setup
- running each service
- API endpoints
- ML pipeline
- dataset source
- testing
- limitations
- future work

---

# 27. PHASE 24 — FINAL PROJECT REPORT CONTENT

The final report should cover:

## Chapter 1 — Introduction

- API security problem
- motivation
- objective
- scope
- **explicit research questions (RQ1, RQ2, RQ3 from Section 0.5)** and one paragraph stating the specific gap this project fills (comparison across unsupervised detectors + adversarial evasion robustness + explainability) relative to existing WAF-ML work

## Chapter 2 — Literature Survey

Review existing work on:

- WAF
- API security
- HTTP anomaly detection
- ML-based intrusion detection
- Isolation Forest
- recent WAF datasets
- adversarial robustness / evasion of ML-based intrusion detection (needed to support RQ2's novelty claim)

For each paper record:

```text
Paper
Year
Problem
Dataset
Method
Result
Limitation
Relevance to our project
```

## Chapter 3 — Dataset and Methodology

- dataset source
- preprocessing
- feature engineering
- model
- evaluation

## Chapter 4 — System Design

- architecture
- request flow
- gateway
- ML service
- database
- dashboard

## Chapter 5 — Implementation

- Spring Boot
- FastAPI
- Isolation Forest
- MongoDB
- React

## Chapter 6 — Results

Only actual measured results. Must include, at minimum:

```text
Cross-validated metrics table: rule-based baseline vs Isolation Forest vs One-Class SVM vs LOF vs Random Forest, with each model's training-data protocol stated (RQ1)
Explainability findings: top contributing features, worked examples (RQ3)
Adversarial evasion ADR/degradation table, per attack category, per approach including the rule baseline (RQ2)
```

## Chapter 7 — Limitations and Future Scope

Be honest about limitations.

---

# 28. PHASE 25 — FINAL PRESENTATION

Suggested slides:

1. Title
2. Problem Statement
3. Motivation
4. Existing System / Limitations
5. Proposed System
6. Literature Survey
7. Dataset
8. System Architecture
9. Methodology
10. ML Model
11. Implementation
12. Results
13. Demo
14. Conclusion
15. Future Scope
16. References

---

# 29. AGENT TASK MANAGEMENT

The coding agent should maintain a file:

```text
TASKS.md
```

Format:

```markdown
# Development Tasks

## Phase 1
- [x] Environment verified

## Phase 2
- [x] Repository created
- [x] Git initialized

## Phase 3
- [x] Gateway created
- [x] Health endpoint created
- [x] Gateway tested

## Phase 4
- [ ] Backend created
- [ ] Backend tested

...
```

Update this after every completed milestone.

---

# 30. HOW THE AGENT SHOULD WORK

For every task:

### Step A — Explain

Before making a major change, briefly explain:

```text
What we are changing
Why we need it
Which files will change
How we will test it
```

### Step B — Implement

Make the smallest reasonable change.

### Step C — Test

Run:

```text
build
unit tests
integration test where applicable
manual curl/API test
```

### Step D — Report

Tell the user:

```text
Completed:
Files changed:
Tests:
Result:
Next phase:
```

### Step E — Stop if something fails

Do not continue building on a broken foundation.

---

# 31. IMPORTANT "FOR KRISHNA" NOTES

These comments are for the student, not instructions to be blindly copied into code.

## Why Spring Boot?

Spring Boot is our Java gateway layer. It handles HTTP requests and provides the structure for proxying, security processing, configuration and APIs.

## Why Python?

Python has a mature ML ecosystem:

```text
Pandas
NumPy
Scikit-learn
FastAPI
```

It is easier to develop and experiment with the anomaly detector separately.

## Why two services?

We separate:

```text
Gateway responsibility
```

from:

```text
Machine-learning responsibility
```

This makes development, testing and future replacement of the model easier.

## Why Isolation Forest?

Because the central ML task is anomaly detection.

The model tries to identify requests that are unusual relative to the learned data distribution.

## Why not use only rule-based detection?

Rule-based detection can identify known patterns effectively, but our project specifically investigates whether ML-based anomaly detection can provide an additional layer for unusual or previously unseen request patterns.

Do not claim that ML replaces WAF rules entirely.

## Why recent data?

The project should prioritize realistic and recent HTTP/WAF traffic where possible. CSIC 2010 can be retained as a benchmark, but it should not be the sole justification for the system.

## Why not train directly on raw HTTP strings?

For the initial mini-project, we use interpretable engineered features so the feature-to-model pipeline is easier to explain and evaluate.

---

# 32. EXPECTED FINAL REQUEST FLOW

The final demonstration should show:

### Normal request

```text
POST /api/login
       ↓
API Security Gateway
       ↓
Feature extraction
       ↓
ML Service
       ↓
NORMAL
       ↓
FORWARD
       ↓
Backend
       ↓
200 OK
```

### Anomalous request

```text
GET /api/search?...suspicious payload...
       ↓
API Security Gateway
       ↓
Feature extraction
       ↓
ML Service
       ↓
ANOMALY
       ↓
BLOCK
       ↓
403 Forbidden
```

### Security logging

```text
Request
  ↓
Prediction
  ↓
Decision
  ↓
Security Event
  ↓
MongoDB
  ↓
Dashboard
```

---

# 33. DEFINITION OF DONE

The project is considered functionally complete only when all of the following work:

- [ ] Gateway starts
- [ ] Backend starts
- [ ] Gateway forwards requests
- [ ] ML model trains successfully
- [ ] Rule-based signature baseline (Phase 14B) is implemented and testable in isolation
- [ ] Isolation Forest, One-Class SVM, LOF, and Random Forest (reference) are all trained and cross-validated on the same features, with each model's training-data protocol documented (RQ1)
- [ ] Train/test leakage checks (duplicates, near-duplicates, template overlap) are done and documented (Phase 8)
- [ ] Explainability results exist for the primary model (RQ3)
- [ ] Adversarial evasion test set exists and ADR/degradation is measured per approach (rule-based + all 4 models), per attack category (RQ2)
- [ ] Model evaluation is reproducible
- [ ] FastAPI `/predict` works
- [ ] Gateway calls ML service
- [ ] Normal requests can reach backend
- [ ] Detected anomalies can be blocked
- [ ] Security events are logged
- [ ] MongoDB persistence works, if included
- [ ] Dashboard displays real system data
- [ ] End-to-end tests pass
- [ ] Project can be started from documented commands
- [ ] No secrets are committed
- [ ] Dataset source and license are documented
- [ ] Experimental metrics are based on actual runs
- [ ] README is complete
- [ ] Final architecture diagram matches implementation

---

# 34. FIRST AGENT PROMPT

When starting the project in Antigravity, do NOT ask the agent to build everything.

Give it this first instruction:

> You are helping develop a B.Tech mini project named "API Security Gateway with Machine Learning-Based Anomaly Detection".
>
> Read `PROJECT_PLAN.md` completely before making changes.
>
> We will implement the project phase by phase. Do not skip phases and do not build the entire application at once.
>
> Start with Phase 1: inspect the current development environment and repository. Do not install or modify anything until you report what is already available.
>
> Then implement only Phase 2 and Phase 3 as appropriate:
> - initialize the repository structure
> - create the Spring Boot gateway
> - use Java 21 and Maven
> - add Spring Web and Actuator
> - create `GET /api/health`
> - run the application
> - test the endpoint
>
> Before each major change, explain what you are changing and why.
>
> After implementation, run the relevant tests/build and report:
> 1. files created/changed
> 2. commands executed
> 3. test results
> 4. any problems
> 5. next recommended phase
>
> Do not proceed to ML, dataset processing, MongoDB, React, or advanced security features yet.

---

# 35. CRITICAL PROJECT PRINCIPLE

The final project should be understandable by the student.

The agent must not create:

- unnecessary microservices
- unnecessary cloud infrastructure
- complex Kubernetes deployment
- excessive design patterns
- huge amounts of generated code
- unexplained dependencies
- fake ML results
- undocumented datasets

The target is a **strong, demonstrable B.Tech mini-project**, not a production enterprise WAF.

The priority is:

```text
Correctness
   ↓
Understandability
   ↓
Reproducibility
   ↓
Security
   ↓
Evaluation
   ↓
Presentation quality
```

---

# 36. CURRENT STATUS

At project start:

```text
Environment:        READY / VERIFY
Repository:         NOT STARTED
Gateway:            NOT STARTED
Backend:            NOT STARTED
Dataset:            TO BE VERIFIED
Preprocessing:      NOT STARTED
Feature engineering:NOT STARTED
ML model:           NOT STARTED
ML API:             NOT STARTED
Gateway integration:NOT STARTED
Logging:            NOT STARTED
MongoDB:            NOT STARTED
Dashboard:          NOT STARTED
Testing:            NOT STARTED
Documentation:      IN PROGRESS
```

Update this section as development progresses.

---

# 37. FINAL REMINDER FOR THE AGENT

**Do not optimize for writing the most code. Optimize for producing a working, understandable and reproducible project.**

The correct progression is:

```text
Working Gateway
      ↓
Working Backend
      ↓
Working Proxy
      ↓
Verified Dataset
      ↓
Working Feature Extraction
      ↓
Working ML Model
      ↓
Measured Evaluation
      ↓
Working ML API
      ↓
Gateway + ML Integration
      ↓
Real Allow/Block Decision
      ↓
Logging
      ↓
Dashboard
      ↓
Testing
      ↓
Documentation
      ↓
Final Demo
```

**Never skip directly from project creation to a supposedly finished AI security system.**
