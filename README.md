# 🧠 SmartFlow AI

> Plateforme intelligente de gestion de tâches basée sur une architecture microservices avec Spring Boot, Spring AI, MCP, Kafka, Telegram et Angular.

## 📌 Présentation

**SmartFlow AI** est un projet fullstack distribué qui permet aux utilisateurs de gérer leurs tâches, interagir avec un assistant IA, et recevoir des notifications en temps réel via Telegram.

L'originalité du projet réside dans l'utilisation du **Model Context Protocol (MCP)** : les services métier (Task, Auth) exposent leurs capacités en tant que **MCP Servers**, et le service IA les consomme en tant que **MCP Client** — c'est l'IA qui décide dynamiquement quel outil appeler en fonction du langage naturel de l'utilisateur.

---

## 🏗️ Architecture

```
┌───────────┐      ┌──────────┐      ┌──────────────────────────┐
│ Telegram  │─────▶│ Gateway  │─────▶│       AI Service         │
│ Bot       │◀─────│ (8080)   │◀─────│       (8083)             │
└───────────┘      └────┬─────┘      │                          │
                        │            │  Spring AI — MCP Client   │
┌───────────┐           │            │                          │
│ Angular   │───────────┘            │  Connecté à :             │
│ (4200)    │                        │  ├─ Task MCP Server       │
└───────────┘                        │  └─ Auth MCP Server       │
                                     └──────┬──────┬─────────────┘
                                            │      │
                             ┌──────────────┘      └───────┐
                             ▼                             ▼
                   ┌──────────────────┐      ┌──────────────────┐
                   │  Task Service    │      │  Auth Service     │
                   │  (8082)          │      │  (8081)           │
                   │                  │      │                   │
                   │  REST + MCP Srv  │      │  REST + MCP Srv   │
                   │  Kafka Producer  │      │  JWT Provider     │
                   └────────┬─────────┘      └──────────────────┘
                            │
                      Kafka (task-events)
                            │
                   ┌────────▼─────────┐
                   │ Notification Svc │
                   │ (8084)           │
                   │ Kafka Consumer   │
                   │ → Telegram Push  │
                   └──────────────────┘

         ┌────────────────┐    ┌─────────────────┐
         │ Config Server  │    │ Discovery Server│
         │ (8888)         │    │ Eureka (8761)   │
         └────────────────┘    └─────────────────┘
```

---

## 🧩 Services

| Service | Port | Rôle | Technos clés |
|---------|------|------|--------------|
| **Discovery Server** | 8761 | Registre des services | Eureka Server |
| **Config Server** | 8888 | Configuration centralisée | Spring Cloud Config + Git |
| **Auth Service** | 8081 | Authentification + MCP Server | Spring Security, JWT, PostgreSQL |
| **API Gateway** | 8080 | Point d'entrée unique + filtre JWT | Spring Cloud Gateway |
| **Task Service** | 8082 | Gestion des tâches + MCP Server | JPA, Kafka Producer, MCP Streamable HTTP |
| **AI Service** | 8083 | Assistant IA + MCP Client | Spring AI, MCP Client |
| **Notification Service** | 8084 | Notifications Telegram | Kafka Consumer, Telegram Bot API |
| **Frontend** | 4200 | Interface utilisateur | Angular 19 |

---

## 🔄 Flux principaux

### 1. Authentification
```
Angular → Gateway → Auth Service → JWT token
```

### 2. Gestion de tâches (REST classique)
```
Angular → Gateway (vérifie JWT) → Task Service → PostgreSQL
                                       │
                                       └─▶ Kafka event → Notification Service → Telegram 🔔
```

### 3. Chat IA via Telegram (MCP)
```
Utilisateur Telegram : "Crée une tâche urgente : déployer en prod"
       │
       ▼
Gateway → AI Service (MCP Client)
       │
       │ L'IA comprend l'intention et appelle le bon MCP Tool
       │
       ▼
Task MCP Server → create_task(title="déployer en prod", priority=URGENT)
       │
       ▼
Réponse : "✅ Tâche créée avec priorité URGENTE"
```

---

## 🛠️ Stack technique

**Backend :**
- Java 21, Spring Boot 3.4
- Spring Cloud (Eureka, Config, Gateway)
- Spring Security + JWT (jjwt 0.12.6)
- Spring AI 1.1.0-M2 + MCP Streamable HTTP
- Apache Kafka
- OpenFeign
- PostgreSQL
- Lombok, Validation

**Frontend :**
- Angular 19

**Infra :**
- Docker & Docker Compose
- Git (pour Config Server)

---

## 🧠 Concepts illustrés

Ce projet est conçu comme un **support pédagogique** couvrant :

- ✅ Architecture microservices avec Spring Cloud
- ✅ Service Discovery (Eureka) + Configuration centralisée (Config Server + Git)
- ✅ API Gateway avec validation JWT
- ✅ Authentification stateless avec Spring Security + JWT
- ✅ Communication événementielle avec Apache Kafka
- ✅ Model Context Protocol (MCP) — Servers & Client avec Streamable HTTP
- ✅ Spring AI pour l'intégration LLM
- ✅ OpenFeign pour la communication inter-services
- ✅ Bot Telegram comme interface conversationnelle
- ✅ Frontend Angular avec authentification et streaming IA
- ✅ Conteneurisation avec Docker Compose



---

<p align="center">
  Fait avec ❤️ et ☕ — 2025
</p>
