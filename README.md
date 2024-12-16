# Sistema Bancario - Servicio de Transferencias

Sistema que gestiona transferencias bancarias entre cuentas con validaciones y reglas de negocio.

### Ruta general
- http://localhost:8080/{aca va ruta de los endpoints}

## Funcionalidades Detalladas

### Transferencias
- Transferencias entre cuentas en PESOS y DOLARES
- Monto mínimo: $100a
- Validación de moneda coincidente entre cuenta origen y transferencia
- Verificación automática de saldo disponible
- Actualización en tiempo real de saldos

### Límites y Controles
- Límite diario de transferencias en PESOS: $500,000
- Control acumulado de transferencias diarias
- Validación de cuenta origen existente
- Validación de cuenta destino existente

### Sistema de Cargos
- PESOS:
  - 2% sobre montos > $1,000,000
  - Cargo calculado sobre el monto total
- DOLARES:
  - 0.5% sobre montos > U$S5,000
  - Cargo calculado sobre el monto total

## Arquitectura del Sistema

### Capas
1. Controladores (API REST)
2. Servicios (Lógica de negocio)
3. Repositorios (Acceso a datos)
4. Modelos y DTOs

### Componentes Principales
- TransferenciaService:
  - Validaciones de negocio
  - Procesamiento de transferencias
  - Cálculo de cargos

- CuentaRepository:
  - Gestión de cuentas
  - Actualización de saldos

- TransaccionRepository:
  - Registro de operaciones
  - Control de límites diarios

### DTOs
- TransferenciaRequestDTO:
  - cuentaOrigen
  - cuentaDestino
  - monto
  - moneda

- TransferenciaResponseDTO:
  - estado
  - mensaje

## Tests Unitarios Implementados

### Escenarios Exitosos
1. Transferencia básica exitosa
2. Transferencia con cargo en PESOS
3. Transferencia con cargo en DOLARES

### Validaciones y Errores
4. Control de límite diario excedido
5. Validación de monto mínimo
6. Verificación de saldo insuficiente
7. Validación de moneda diferente

### Uso
- mvn clean install
- mvn test
- mvn spring-boot:run
