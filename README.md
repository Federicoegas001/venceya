# VenceYa

Gestor de vencimientos para IT y pymes: dominios, certificados SSL, licencias y contratos de mantenimiento. Un job diario revisa qué está por vencer y avisa por mail al responsable a los **30, 15, 7 y 1 día**, el día del vencimiento y todos los días si ya venció.

**Demo interactiva:** en mi portfolio (sección Proyectos → VenceYa).

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.3 · Spring Web · Spring Data JPA · Bean Validation |
| Tareas programadas | Spring Scheduler (`@Scheduled`, cron) |
| Base de datos | MySQL 8.4 (H2 en los tests) |
| Mails | JavaMailSender → AWS SES (SMTP) · Mailpit en local |
| Infra | Docker · Docker Compose · GitHub Actions · AWS EC2 + RDS + SES |

## Cómo funciona el aviso

`PoliticaDeAlertas` decide, según los días que faltan y el último umbral avisado, si hoy corresponde mandar un mail. Así el job puede correr todos los días sin repetir avisos:

| Faltan | Último aviso | ¿Avisa hoy? |
|---|---|---|
| 20 días | ninguno | Sí, umbral 30 |
| 12 días | 15 | No |
| 7 días | 15 | Sí, umbral 7 |
| −3 días | cualquiera | Sí, aviso diario de vencido |

Al renovar (`PATCH /api/vencimientos/{id}/renovar`) la fecha se corre un año y los avisos se reinician.

## Correr en local

```bash
docker compose up -d --build
```

- API: http://localhost:8080/api/vencimientos
- Bandeja de mails (Mailpit): http://localhost:8025

Cargar un vencimiento y disparar el job a mano:

```bash
curl -X POST localhost:8080/api/vencimientos -H "Content-Type: application/json" \
  -d '{"nombre":"midominio.com.ar","tipo":"DOMINIO","fechaVencimiento":"2026-11-10","responsableEmail":"it@empresa.com","proveedor":"NIC Argentina","costoAnual":18000}'

curl -X POST localhost:8080/api/vencimientos/alertas/ejecutar
```

## Tests

```bash
mvn verify
```

- `PoliticaDeAlertasTest`: lógica de umbrales (parametrizado).
- `VencimientoApiTest`: alta, validaciones, 404 y que el job no repita avisos, con MockMvc y H2.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/vencimientos?tipo=DOMINIO` | Listar (filtro opcional por tipo) |
| GET | `/api/vencimientos/resumen` | Cantidad por estado y costo anual total |
| GET | `/api/vencimientos/{id}` | Detalle |
| POST | `/api/vencimientos` | Crear |
| PUT | `/api/vencimientos/{id}` | Editar |
| PATCH | `/api/vencimientos/{id}/renovar` | Renovar un año |
| DELETE | `/api/vencimientos/{id}` | Eliminar |
| POST | `/api/vencimientos/alertas/ejecutar` | Correr el job a mano |

## Deploy en AWS

1. **RDS MySQL** (db.t3.micro, capa gratuita) en una subred privada. Security group que solo acepte 3306 desde el de la EC2.
2. **SES**: verificar el dominio o el mail remitente y crear credenciales SMTP.
3. **EC2** (t2.micro, Amazon Linux 2023) con Docker. Correr la imagen con las variables:

```bash
docker run -d -p 80:8080 \
  -e DB_URL=jdbc:mysql://<endpoint-rds>:3306/venceya \
  -e DB_USER=venceya -e DB_PASSWORD=<pass> \
  -e SMTP_HOST=email-smtp.us-east-1.amazonaws.com -e SMTP_PORT=587 \
  -e SMTP_USER=<user-ses> -e SMTP_PASSWORD=<pass-ses> \
  -e SMTP_AUTH=true -e SMTP_TLS=true \
  -e MAIL_FROM=alertas@midominio.com \
  venceya:latest
```

Las credenciales van como variables de entorno (o en AWS Secrets Manager), nunca en el repo.

## Próximos pasos

- Login con Spring Security + JWT y multiempresa.
- Aviso por WhatsApp como segundo `Notificador`.
- Chequeo automático de la fecha real de los certificados SSL.
