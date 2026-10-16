# MediSalud - Agendamiento de citas médicas

API para agendar citas médicas. Se pueden registrar médicos y pacientes, ver los horarios libres de un médico, reservar una cita, cancelarla y reprogramarla.

Está hecha con Java 17 y Spring Boot, y usa una base de datos H2 en memoria, así que no hay que instalar nada extra. Eso sí: los datos se borran cada vez que se reinicia la app.

## Cómo correrla

### Con Gradle

Solo necesitas tener Java 17 instalado.

```bash
./gradlew bootRun
```

En Windows: `.\gradlew.bat bootRun`

### Con Docker

```bash
docker compose up --build
```

### Qué abrir

- Swagger: http://localhost:8080/swagger-ui.html
- La API vive en: http://localhost:8080/api

Al arrancar se crean 3 médicos de ejemplo para poder probar de una vez

## Cómo la subí a AWS

Está montada con CloudFormation en la carpeta `infra/`:

- `ecr.yml` crea el repositorio donde se guarda la imagen de Docker.
- `app.yml` levanta la app en ECS Fargate con un balanceador de carga para tener una URL pública.

Los pasos fueron:

```powershell
# 1. Crear el repositorio
aws cloudformation deploy --template-file infra/ecr.yml --stack-name medisalud-ecr

# 2. Construir y subir la imagen
docker build -t <uri-del-repositorio>:latest .
docker push <uri-del-repositorio>:latest

# 3. Desplegar la app
aws cloudformation deploy --template-file infra/app.yml --stack-name medisalud-app --capabilities CAPABILITY_IAM --parameter-overrides file://params.json
```

El archivo `params.json` se ve así:

```json
{
  "Parameters": {
    "ImageUri": "<uri-del-repositorio>:latest",
    "VpcId": "<vpc>",
    "SubnetIds": "<subnet-1>,<subnet-2>"
  }
}
```

Al terminar, la URL sale en los outputs del stack `medisalud-app`. Para borrar todo: `aws cloudformation delete-stack --stack-name medisalud-app`.

## Reglas del negocio

- **Horario:** lunes a viernes de 8:00 a 18:00 y sábados de 8:00 a 13:00. Domingos y festivos de Colombia no se atiende.

- **Citas:** duran 30 minutos y empiezan en punto o y media. No se puede agendar en el pasado.

- **Sin cruces:** un médico no puede tener dos citas a la misma hora, y un paciente tampoco ni siquiera con médicos distintos.

- **Fecha de nacimiento:** es opcional, pero no puede ser una fecha futura.

- **Cancelar:** si se cancela con menos de 2 horas de anticipación, el paciente recibe una penalización.7

- **Bloqueo:** con 3 penalizaciones en los últimos 30 días, el paciente no puede agendar más.

- **Reprogramar:** cancela la cita vieja y crea una nueva en un solo paso. Si el horario nuevo no sirve, la cita original queda como estaba.

## Endpoints

| Qué hace | Endpoint |
|---|---|
| Registrar médico | `POST /api/medicos` |
| Ver médicos | `GET /api/medicos` y `GET /api/medicos/{id}` |
| Registrar paciente | `POST /api/pacientes` |
| Ver pacientes | `GET /api/pacientes` y `GET /api/pacientes/{id}` |
| Agendar cita | `POST /api/citas` |
| Ver horarios libres | `GET /api/citas/disponibles?medicoId=1&fechaInicio=02-06-2026&fechaFin=02-06-2026` |
| Listar citas (con filtros) | `GET /api/citas` |
| Ver una cita | `GET /api/citas/{id}` |
| Cancelar cita | `PATCH /api/citas/{id}/cancelar` |
| Reprogramar cita | `POST /api/citas/{id}/reprogramar` |

Ejemplo para agendar una cita:

```json
{ "pacienteId": 1, "medicoId": 1, "fechaHora": "02-06-2026 9:00" }
```

Las fechas van como `dd-MM-yyyy` (por ejemplo `02-06-2026`) y con hora como `dd-MM-yyyy H:mm` (por ejemplo `02-06-2026 9:00`). La hora es la de Colombia.

## Errores

Cuando algo sale mal, la API responde con el código HTTP correspondiente y un cuerpo así:

```json
{
  "timestamp": "2026-06-01T10:00:00-05:00",
  "status": 409,
  "error": "Conflict",
  "code": "DOCTOR_SLOT_TAKEN",
  "message": "El médico ya tiene una cita programada en esa franja",
  "path": "/api/citas"
}
```

- `400`: datos mal enviados, horario fuera de atención o cita en el pasado.
- `404`: el médico, paciente o cita no existe.
- `409`: conflicto, por ejemplo horario ocupado, documento repetido o paciente bloqueado.
- `500`: error inesperado.

## Cómo está organizado el código

Separé el proyecto en capas para que las reglas del negocio no dependan de Spring ni de la base de datos:

```
src/main/java/com/ceiba/test
├── domain          reglas del negocio
├── application     casos de uso (lo que hace la app)
├── infrastructure  base de datos, festivos y configuración
└── presentation    controladores REST y manejo de errores
```

## Pruebas

```bash
./gradlew test
```

Hay pruebas de las reglas del negocio, de los casos de uso y de la API completa (con H2 y una fecha fija para que den siempre el mismo resultado).
