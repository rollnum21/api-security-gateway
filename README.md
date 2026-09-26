# API Security Gateway with Machine Learning-Based Anomaly Detection

> **B.Tech Mini Project**  
> An API Security Gateway that intercepts HTTP requests, extracts security-relevant features, and uses machine-learning anomaly detection to allow or block traffic before it reaches the target backend.

---

## Architecture Overview

```
Client (Browser / Postman / cURL)
        │
        ▼
API Security Gateway (Spring Boot :8080)
        │  — intercepts, validates, captures metadata
        ▼
ML Service (Python FastAPI :8000)
        │  — feature extraction → Isolation Forest prediction
        ▼
 ┌──────┴──────┐
NORMAL       ANOMALY
  │              │
Forward        Block
  │             (403)
  ▼
Target Backend API (Spring Boot :3000)
        │
        ▼
Security Events → MongoDB → React Dashboard
```

---

## Components

| Component | Technology | Port | Status |
|---|---|---|---|
| API Security Gateway | Java 21 / Spring Boot 3 | 8080 | 🚧 In progress |
| Target Backend | Java 21 / Spring Boot 3 | 3000 | ⏳ Pending |
| ML Service | Python 3 / FastAPI | 8000 | ⏳ Pending |
| MongoDB | MongoDB | 27017 | ⏳ Pending |
| Dashboard | React / Vite | 5173 | ⏳ Pending |

---

## Prerequisites

- Java 21 (OpenJDK)
- Apache Maven 3.6+
- Python 3.10+
- pip
- Node.js 20+ and npm
- MongoDB (later phase)
- cURL or Postman

---

## Quick Start

### 1. API Security Gateway

```bash
cd gateway
mvn spring-boot:run
```

Health check:
```bash
curl http://localhost:8080/api/health
```

### 2. Target Backend *(pending)*

```bash
cd backend
mvn spring-boot:run
```

### 3. ML Service *(pending)*

```bash
cd ml-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

---

## API Endpoints

### Gateway (`:8080`)

| Method | Path | Description |
|---|---|---|
| GET | `/api/health` | Gateway health check |

### Backend (`:3000`) *(pending)*

| Method | Path | Description |
|---|---|---|
| GET | `/api/backend/health` | Backend health check |
| GET | `/api/products` | Products list |
| GET | `/api/users` | Users list |

### ML Service (`:8000`) *(pending)*

| Method | Path | Description |
|---|---|---|
| GET | `/health` | ML service health check |
| POST | `/predict` | Anomaly prediction |

---

## Project Structure

```
api-security-gateway/
├── gateway/          Java Spring Boot gateway
├── backend/          Java Spring Boot target backend
├── ml-service/       Python FastAPI ML anomaly detector
│   ├── app/
│   ├── model/        Pre-trained model artifacts (.joblib)
│   └── tests/
├── dataset/
│   ├── raw/          Original dataset (git-ignored)
│   ├── processed/    Cleaned CSV after preprocessing
│   └── scripts/      Python preprocessing scripts
├── frontend/         React dashboard (later)
├── docs/
│   ├── architecture/
│   ├── experiments/
│   └── literature/
├── .gitignore
├── TASKS.md
├── README.md
└── PROJECT_PLAN.md
```

---

## ML Pipeline

> **Note:** Heavy ML training (cross-validation, model comparison, adversarial evasion experiments) is performed in Google Colab. Only pre-trained `.joblib` model artifacts are used at runtime on this machine.

Models evaluated (RQ1):
- Rule-based signature baseline (regex)
- Isolation Forest (primary, unsupervised)
- One-Class SVM (unsupervised)
- Local Outlier Factor / LOF (unsupervised)
- Random Forest (supervised reference ceiling)

---

## Research Questions

- **RQ1:** How do unsupervised anomaly detectors compare against a rule-based baseline and supervised reference across precision/recall/F1?
- **RQ2:** How much does each approach degrade when attacks are obfuscated (encoding, whitespace injection, LLM-paraphrased variants)?
- **RQ3:** Which features drive the anomaly decision (global permutation importance + SHAP)?

---

## Dataset

> Dataset source and license to be documented in `dataset/README.md` (Phase 7).

---

## Limitations

- ML training performed offline in Colab; laptop used only for inference.
- Adversarial evasion testing is an offline research experiment, not a live gateway feature.
- MongoDB and React dashboard are optional portfolio components.

---

## License

B.Tech Mini Project — for academic use only.
