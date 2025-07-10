# Bamboo Notificator
![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1-blue) ![Docker](https://img.shields.io/badge/Docker-20.10%2B-2496ED)

Система мониторинга сборок Bamboo с уведомлениями в Telegram и Pachca.
## Содержание
- [Требования](#-требования)
- [Установка](#-установка)
  - [1️⃣ Локальный запуск (без Docker)](#1️⃣-локальный-запуск-без-docker)
  - [2️⃣ Запуск с Docker](#2️⃣-запуск-с-docker)
- [Конфигурация](#️-конфигурация)
- [Разработка](#-разработка)
##  Требования
- Java **17+**
- Maven **3.8+**
- Docker **20.10+** (опционально)
- Redis **7.2+** (для хранения состояния)
## Установка
### 1️⃣ Локальный запуск (без Docker)
```bash
git clone https://github.com/Algeron1/BambooNotificator.git
cd BambooNotificator
cp .env.example .env
# Отредактируйте .env файл под свои настройки
BAMBOO_API_URL=https://bamboo.server.ru/rest/api/latest/
BAMBOO_API_LOGIN=bamboo username
BAMBOO_API_PASSWORD=bamboo api password
ADMIN_USERNAME=admin username
ADMIN_PASSWORD=admin password
TELEGRAM_API_URL=https://api.telegram.org/token/sendMessage
TELEGRAM_CHAT_ID=chat id
PACHKA_API_URL=https://api.pachca.com/api/shared/v1/messages/
PACHKA_TOKEN=pachka token
PACHKA_ENTITY_ID=pachka entity id
REDIS_HOST=localhost
REDIS_PORT=6379
docker run -d --name redis -p 6379:6379 redis:7.2-alpine
mvn clean package
java -jar target/*.jar
```
### 2️⃣ Запуск с Docker
```bash
git clone https://github.com/Algeron1/BambooNotificator.git
cd BambooNotificator
cp .env.example .env
nano .env
docker-compose up -d --build
docker-compose logs -f app
```
## Конфигурация
Основные файлы конфигурации:
### `.env`
```ini
BAMBOO_API_URL=https://your.bamboo.instance
BAMBOO_API_LOGIN=username
BAMBOO_API_PASSWORD=password
TELEGRAM_API_URL=https://api.telegram.org/botTOKEN/sendMessage
TELEGRAM_CHAT_ID=-1001234567890
```
### `application.yml`
```yaml
bamboo:
  deploymentIds:
    12345: "Project Name"
    67890: "Another Project"
```
