# Healink-SOS
**Sistema de Monitoreo y Alerta para Adultos Mayores**

HeaLink SOS es una solución integral de salud que combina movilidad, backend escalable y automatización DevOps. Diseñada bajo una arquitectura de microservicios, permite el monitoreo constante de signos vitales, gestión de medicamentos y alertas de emergencia en tiempo real.

## Funciones principales

* **Autenticación de usuarios**: Gestión del inicio de sesión y validación de acceso al sistema.
* **Gestión de perfil**: Registro y edición de información del usuario, incluyendo datos médicos críticos como tipo de sangre, alergias e historial clínico.
* **Administración de contactos**: Configuración de números de emergencia con asignación de niveles de prioridad para la respuesta ante crisis.
* **Control de medicinas**: Registro y almacenamiento de medicamentos y tratamientos activos para referencia médica.
* **Módulo SOS**: Función principal que recibe telemetría (BPM y ubicación) y dispara automáticamente llamadas de voz y mensajes SMS mediante Twilio al detectar una emergencia.
* **Persistencia híbrida**: Almacenamiento de perfiles y contactos en bases de datos relacionales (Oracle SQL) y del historial de biometría en bases de datos de alta velocidad (Oracle NoSQL).

## Stack Tecnológico Principal

* **Aplicación Móvil y Simulación**: Desarrollo en Java para la captura y envío de telemetría.
* **Backend API**: TypeScript con Node.js, Express y TypeORM.
* **Comunicaciones**: Twilio API para la gestión de mensajes SMS y llamadas de voz.
* **Bases de datos**: Oracle DB para el manejo de información relacional y Oracle NoSQL para el almacenamiento de alta velocidad de telemetría.
* **DevOps e Infraestructura**: Despliegue preparado para Ubuntu Sever, Docker y Kubernetes.