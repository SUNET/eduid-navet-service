# eduid-navet-service

SOAP-to-REST wrapper that exposes Swedish Tax Agency (Skatteverket) Navet population registry services as JSON REST APIs. Part of the [eduID](https://eduid.se) infrastructure operated by [SUNET](https://www.sunet.se).

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/personpost/person` | Lookup person by identity number |
| POST | `/personpost/navetnotification` | Full Navet response with relations, deregistration, etc. |
| POST | `/namnsokning` | Search by name/city |

## Build

Requires Java 11 and Maven.

```bash
mvn package
```

This produces an executable fat JAR in `target/`.

## Run

```bash
java -jar target/eduid-navet-service-0.1-SNAPSHOT.jar -c navet-service.properties
```

See `navet-service.properties` for configuration options (Navet endpoint, mutual TLS keystores, server binding, optional Basic Auth).

## Docker

```bash
docker build -t eduid-navet-service .
```

The container startup script (`docker/start.sh`) converts PEM certificates into Java keystores automatically. Mount config and certs at `/opt/eduid/eduid-navet-service/etc`.

## License

BSD 3-Clause
