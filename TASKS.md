
# Development Tasks — API Security Gateway

> Tracking file for all project phases.
> Updated after every completed milestone.
> ML training phases will be completed in Google Colab.

---

## Phase 1 — Environment Verification
- [x] Java 21 verified (OpenJDK 21.0.12.1)
- [x] Maven 3.6.3 verified
- [x] Python 3.10.12 verified
- [x] pip 22.0.2 verified
- [x] Git 2.34.1 verified
- [x] Node.js v22.19.0 verified
- [x] npm 10.9.3 verified
- [x] cURL verified
- [x] FastAPI + uvicorn already installed
- [ ] MongoDB — not installed (later phase)

**Notes:**
- Heavy ML training (scikit-learn models) will be done in Google Colab.
- Only pre-trained `.joblib` artifacts will be loaded on this machine.

---

## Phase 2 — Initialize Repository
- [x] Git repository initialized (`git init`)
- [x] Folder structure created (gateway, backend, ml-service, dataset, frontend, docs)
- [x] `.gitignore` created
- [x] `README.md` created
- [x] `TASKS.md` created (this file)

---

## Phase 3 — Spring Boot Gateway
- [x] Spring Boot 3.3.5 project created in `gateway/`
- [x] Maven + Java 21 configured
- [x] Dependencies: spring-boot-starter-web, spring-boot-starter-actuator
- [x] `GET /api/health` endpoint implemented
- [x] Application starts on port 8080
- [x] curl health check passed
- [x] `gateway/README.md` created

---

## Phase 4 — Target Backend API
- [x] Spring Boot project created in `backend/`
- [x] Port 3000 configured
- [x] `GET /api/backend/health` implemented
- [x] `GET /api/products` implemented
- [x] `GET /api/users` implemented
- [x] Endpoints tested with curl

---

## Phase 5 — Gateway → Backend Proxy
- [x] Proxy routing implemented in gateway
- [x] `curl http://localhost:8080/api/products` returns backend response
- [x] Proxy tested end-to-end

---

## Phase 6 — Request Interception Pipeline
- [ ] Request interceptor/filter created
- [ ] Metadata extraction: timestamp, method, path, query, content-type, content-length, IP
- [ ] Security processing placeholder in place (always ALLOW for now)

---

## Phase 7 — Dataset Finalization
- [ ] Dataset candidate(s) evaluated
- [ ] Dataset selected and documented in `dataset/README.md`
- [ ] Attack taxonomy defined (SQLi, XSS, path traversal, command injection)
- [ ] Sample loaded successfully with Python

---

## Phase 8 — Data Preprocessing
- [ ] `dataset/scripts/inspect_dataset.py` created
- [ ] `dataset/scripts/preprocess_dataset.py` created
- [ ] Schema inspected and documented
- [ ] Leakage checks performed (duplicates, near-duplicates, template overlap)
- [ ] `dataset/processed/processed_requests.csv` generated

---

## Phase 9 — Feature Engineering
- [ ] `ml-service/app/feature_extractor.py` created
- [ ] Feature set documented and justified
- [ ] Feature extraction tested on sample data

---

## Phase 10 — Model Training (Google Colab)
- [ ] Isolation Forest trained on normal traffic only
- [ ] One-Class SVM trained on normal traffic only
- [ ] LOF trained on normal traffic only
- [ ] Random Forest trained on labelled data (reference ceiling)
- [ ] Model artifacts exported as `.joblib`
- [ ] Training protocol documented

---

## Phase 11 — Model Evaluation (Google Colab)
- [ ] 5-fold cross-validation completed for all models
- [ ] Metrics: Precision, Recall, F1, Confusion Matrix per model (mean ± std)
- [ ] RQ1 comparison table created
- [ ] `docs/experiments/model-evaluation.md` updated

---

## Phase 11B — Explainability (Google Colab)
- [ ] Permutation feature importance computed
- [ ] SHAP values computed (if time allows)
- [ ] `docs/experiments/explainability.md` created
- [ ] RQ3 answered

---

## Phase 12 — Python FastAPI ML Service
- [ ] `ml-service/app/main.py` created
- [ ] `ml-service/app/schemas.py` created
- [ ] `ml-service/app/predictor.py` created
- [ ] `ml-service/requirements.txt` created
- [ ] `GET /health` returns 200
- [ ] `POST /predict` returns prediction + anomaly score
- [ ] Service tested with curl

---

## Phase 13 — Gateway ↔ ML Service Integration
- [ ] Gateway calls FastAPI `/predict` for every request
- [ ] ML client created in gateway
- [ ] Failure policy defined and documented (fail-closed)
- [ ] End-to-end flow tested

---

## Phase 14 — Security Decision Engine
- [ ] `SecurityDecisionService` created
- [ ] ALLOW / BLOCK logic implemented
- [ ] Threshold configurable via `application.properties`
- [ ] Blocking returns 403

---

## Phase 14B — Rule-Based Signature Baseline
- [ ] `RuleBasedDetector` implemented (regex patterns for SQLi, XSS, path traversal, command injection)
- [ ] Patterns documented in `docs/architecture/rule-baseline.md`
- [ ] Baseline tested against clean and obfuscated attacks

---

## Phase 15 — Security Logging
- [ ] Structured security event logging implemented
- [ ] Log fields: timestamp, requestId, method, path, prediction, anomalyScore, action, processingTime
- [ ] No sensitive data logged

---

## Phase 16 — MongoDB (Optional)
- [ ] MongoDB installed/connected
- [ ] `SecurityEvent` document model created
- [ ] `SecurityEventRepository` created
- [ ] Events persisted to MongoDB

---

## Phase 17 — Monitoring API
- [ ] `GET /api/security/stats` implemented
- [ ] `GET /api/security/events` implemented

---

## Phase 18 — React Dashboard (Optional)
- [ ] Vite + React project created in `frontend/`
- [ ] Dashboard displays total/allowed/blocked counts
- [ ] Recent security events table
- [ ] Connected to gateway monitoring API

---

## Phase 19 — End-to-End Testing
- [ ] Gateway health: 200
- [ ] Backend health: 200
- [ ] Gateway forwarding: passes
- [ ] Normal request: ALLOWED
- [ ] Suspicious request: BLOCKED (403)
- [ ] ML service failure: fail-closed verified
- [ ] Backend failure: gateway returns controlled error
- [ ] Performance: latency measured

---

## Phase 20 — Adversarial Evasion Testing (Google Colab)
- [ ] Obfuscated attack variants generated (encoding, whitespace, LLM-paraphrased)
- [ ] ADR measured per approach per attack category
- [ ] Degradation table created (rule-based vs all ML models)
- [ ] `docs/experiments/adversarial-evasion.md` created
- [ ] RQ2 answered

---

## Phase 21 — Security & Engineering Improvements
- [ ] Rate limiting (optional)
- [ ] Request size limits
- [ ] Timeout handling (Gateway → ML)
- [ ] Circuit breaker (optional)
- [ ] Environment variable configuration

---

## Phase 22 — Dockerization (Optional)
- [ ] `docker-compose.yml` created
- [ ] All services containerized

---

## Phase 23 — Documentation
- [ ] `docs/architecture.md` complete
- [ ] `docs/methodology.md` complete
- [ ] `docs/experiments/model-evaluation.md` complete
- [ ] `README.md` complete

---

## Phase 24 — Final Report
- [ ] Chapter 1: Introduction + RQ1/RQ2/RQ3
- [ ] Chapter 2: Literature Survey
- [ ] Chapter 3: Dataset and Methodology
- [ ] Chapter 4: System Design
- [ ] Chapter 5: Implementation
- [ ] Chapter 6: Results (actual measured)
- [ ] Chapter 7: Limitations and Future Scope

---

## Phase 25 — Final Presentation
- [ ] Slides prepared
- [ ] Demo recorded
