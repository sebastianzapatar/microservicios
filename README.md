# 🍽️ Microservicios — Plataforma de Pedidos

Arquitectura de microservicios con Spring Boot, Kafka y PostgreSQL. Incluye dos modos de ejecución: **Docker Compose** (desarrollo rápido) y **Kubernetes** (producción).

---

## 📐 Arquitectura

```
                          ┌─────────────────┐
               HTTP       │   API Gateway   │  :8089 / NodePort :30089
         ───────────────► │  (apigetaway)   │
                          └────────┬────────┘
                                   │ Enruta por path
                    ┌──────────────┴──────────────┐
                    ▼                             ▼
         ┌──────────────────┐         ┌──────────────────┐
         │  product-service │ :8084   │  order-service   │ :8083
         │   PostgreSQL DB  │         │   PostgreSQL DB   │
         └────────┬─────────┘         └────────┬─────────┘
                  │         Kafka               │
                  └────────── :9092 ────────────┘
                                   │
                            ┌──────┴──────┐
                            │  Zookeeper  │ :2181
                            └─────────────┘

      ┌─────────────────┐        ┌──────────────────┐
      │  Config Server  │ :8888  │  Eureka Server   │ :8761
      │   (serversc)    │        │  (descubrimiento)│
      └─────────────────┘        └──────────────────┘
```

### Rutas del API Gateway

| Path | Servicio destino |
|------|-----------------|
| `/api/products/**` | product-service :8084 |
| `/api/orders/**` | order-service :8083 |

---

## 🧩 Servicios

| Servicio | Puerto | Descripción |
|---|---|---|
| `serversc` | 8888 | Config Server — centraliza la configuración de todos los servicios |
| `eureka` | 8761 | Service Discovery — registro y localización de microservicios |
| `apigetaway` | 8089 | API Gateway — punto de entrada único |
| `product-service` | 8084 | CRUD de productos con base de datos propia |
| `order-service` | 8083 | Gestión de pedidos, consume eventos de productos vía Kafka |
| `postgres` | 5432 | Base de datos relacional (DBs: `products`, `orders`) |
| `kafka` | 9092 | Mensajería asíncrona entre servicios |
| `zookeeper` | 2181 | Coordinador requerido por Kafka |

---

## 🐳 Ejecución con Docker Compose

Ideal para **desarrollo local** — levanta todo con un solo comando.

### Prerrequisitos
- Docker Desktop con Docker Compose

### Comandos

```bash
# Construir imágenes y levantar todos los servicios
docker compose up --build

# Verificar que todos estén corriendo
docker compose ps

# Ver logs de un servicio específico
docker compose logs -f product-service

# Detener todo
docker compose down

# Detener y eliminar volúmenes (borra los datos de PostgreSQL)
docker compose down -v
```

Una vez levantado, el API Gateway está disponible en `http://localhost:8089`.

---

## ☸️ Despliegue en Kubernetes

Para **Docker Desktop** (Kubernetes activado en Settings → Kubernetes) o **minikube**.

### Prerrequisitos
- `kubectl` instalado
- Docker Desktop con Kubernetes habilitado **o** minikube

### Paso 1 — Construir las imágenes locales

```bash
# Docker Desktop
bash k8s/build-images.sh

# minikube
bash k8s/build-images.sh --minikube
```

> El script construye las 5 imágenes Spring Boot y, si usas minikube, las carga automáticamente en el clúster.

### Paso 2 — Aplicar los manifiestos

```bash
kubectl apply -k k8s/
```

Este comando aplica **todos los recursos** del namespace `microservices` en el orden correcto.

### Paso 3 — Verificar el despliegue

```bash
# Ver el estado de los pods en tiempo real
kubectl get pods -n microservices -w
```

Estado esperado tras ~2-3 minutos:

```
NAME                               READY   STATUS    RESTARTS
postgres-0                         1/1     Running   0
zookeeper-xxx                      1/1     Running   0
kafka-xxx                          1/1     Running   0
serversc-xxx                       1/1     Running   0
eureka-xxx                         1/1     Running   0
product-service-xxx                1/1     Running   0
order-service-xxx                  1/1     Running   0
apigetaway-xxx                     1/1     Running   0
```

### Acceder al API Gateway

```bash
# El API Gateway está expuesto como NodePort
curl http://localhost:30089/api/products
curl http://localhost:30089/api/orders
```

### Estructura de manifiestos (`k8s/`)

```
k8s/
├── namespace.yaml                         # Namespace: microservices
├── kustomization.yaml                     # Aplica todo con kubectl apply -k
├── build-images.sh                        # Script de build de imágenes
├── postgres/   secret · pvc · statefulset · service
├── zookeeper/  deployment · service
├── kafka/      deployment · service
├── serversc/   deployment · service
├── eureka/     deployment · service
├── product-service/  deployment · service
├── order-service/    deployment · service
└── apigetaway/       deployment · service (NodePort :30089)
```

---

## 🔧 Comandos útiles de Kubernetes

```bash
# Ver todos los recursos del namespace
kubectl get all -n microservices

# Ver logs de un pod
kubectl logs -n microservices deploy/order-service -f
kubectl logs -n microservices deploy/serversc -f

# Describir un pod para diagnóstico
kubectl describe pod -n microservices <nombre-del-pod>

# Ver eventos (útil si un pod no arranca)
kubectl get events -n microservices --sort-by='.lastTimestamp'

# Eliminar todo el despliegue
kubectl delete namespace microservices
```

---

## 📁 Estructura del repositorio

```
implementacion/
├── compose.yml               ← Docker Compose (todos los servicios)
├── k8s/                      ← Manifiestos de Kubernetes
├── serversc/                 ← Config Server (Spring Cloud Config)
│   └── src/main/resources/
│       └── config/           ← YAMLs de configuración por servicio
├── eureka/                   ← Eureka Server
├── apigetaway/               ← API Gateway (Spring Cloud Gateway)
├── product-service/          ← Microservicio de productos
└── order-service/            ← Microservicio de pedidos
```

---

## ⚙️ Variables de entorno clave

| Variable | Valor en contenedor |
|---|---|
| `SPRING_CONFIG_IMPORT` | `configserver:http://serversc:8888` |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | `http://eureka:8761/eureka/` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/<db>` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092` |
