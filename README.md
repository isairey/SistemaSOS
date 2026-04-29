# 🏥🚨 HeaLink SOS  
### Sistema Inteligente de Monitoreo y Alerta para Adultos Mayores

**HeaLink SOS** es una plataforma de salud digital diseñada para el **monitoreo continuo, prevención y respuesta ante emergencias médicas**, enfocada en adultos mayores.  

Integra aplicaciones móviles, backend escalable y arquitectura de microservicios para ofrecer una solución robusta, segura y en tiempo real.

---

## 📌 Descripción

HeaLink SOS permite supervisar el estado de salud de los usuarios mediante la recopilación de datos biométricos y su análisis en tiempo real.

El sistema está diseñado para actuar de forma **automática ante situaciones críticas**, notificando a contactos de emergencia mediante llamadas y mensajes.

---

## 🎯 Objetivo

Brindar una solución tecnológica que permita:

- ❤️ Monitoreo continuo de signos vitales  
- 🚨 Respuesta inmediata ante emergencias  
- 👨‍👩‍👧‍👦 Conexión con contactos de confianza  
- 📊 Gestión integral de información médica  

---

## 🚀 Funcionalidades principales

### 🔐 Autenticación de usuarios
- Registro e inicio de sesión seguro  
- Validación de acceso  

### 👤 Gestión de perfil
- Información personal y médica  
- Datos críticos: tipo de sangre, alergias, historial clínico  

### 📞 Contactos de emergencia
- Configuración de contactos  
- Priorización de respuesta  

### 💊 Control de medicamentos
- Registro de tratamientos  
- Seguimiento de medicamentos activos  

### 🚨 Módulo SOS (Core del sistema)
- Recepción de telemetría (BPM, ubicación)  
- Detección automática de eventos críticos  
- Activación de alertas en tiempo real  
- Envío de SMS y llamadas mediante Twilio  

### 💾 Persistencia híbrida
- Datos estructurados en bases relacionales  
- Telemetría en bases de alta velocidad  

---

## 🧠 Arquitectura del sistema

HeaLink SOS está basado en una arquitectura de **microservicios**, lo que permite:

- ⚡ Escalabilidad  
- 🔄 Alta disponibilidad  
- 🔐 Seguridad  
- 🧩 Modularidad  

---

## 🛠️ Stack tecnológico

### 📱 Aplicación móvil
- **Java** → Captura y envío de datos biométricos  

### ⚙️ Backend
- **Node.js**  
- **TypeScript**  
- **Express**  
- **TypeORM**  

### 📡 Comunicaciones
- **Twilio API** → SMS y llamadas de emergencia  

### 🗄️ Bases de datos
- **Oracle SQL** → Información estructurada  
- **Oracle NoSQL** → Telemetría en tiempo real  

### ☁️ DevOps & Infraestructura
- **Docker** → Contenedorización  
- **Kubernetes** → Orquestación  
- **Ubuntu Server** → Entorno de despliegue  

---

## 🔄 Flujo de funcionamiento

1. 📱 El dispositivo móvil captura datos biométricos  
2. 📡 Se envían al backend en tiempo real  
3. 🧠 El sistema analiza condiciones críticas  
4. 🚨 Se activa el protocolo SOS si es necesario  
5. 📞 Se notifican contactos mediante SMS/llamadas  
6. 📊 Se registra el evento en la base de datos  

---

## 🎨 Características destacadas

- ⚡ Respuesta en tiempo real  
- 🔐 Seguridad de datos médicos  
- 📱 Interfaz intuitiva  
- 🌐 Sistema escalable  
- 🤖 Automatización de emergencias  

---

## 📈 Futuro del proyecto

- 🤖 Integración con IA para predicción de riesgos  
- ⌚ Compatibilidad con wearables  
- 📊 Dashboard clínico avanzado  
- 🔔 Notificaciones push  
- ☁️ Despliegue en cloud (AWS / Azure / GCP)  

---

## ⚠️ Nota

Este sistema es una herramienta de apoyo y **no sustituye atención médica profesional**.

---

## 📄 Licencia

Proyecto de uso académico y demostrativo.

---

## 👨‍💻 Autor

Desarrollado por **Isai Reyes Peña**
