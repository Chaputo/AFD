# AFD System

Aplicación de escritorio en Java (Swing) para la gestión comercial de un negocio con varias sucursales. Permite administrar pedidos, ventas, stock, órdenes de compra, proveedores, clientes, productos y usuarios, con acceso según el rol de cada usuario.

## Funcionalidades

- **Pedidos y operaciones**: carga y seguimiento de pedidos por sucursal.
- **Inventario**: stock por sucursal y consolidado, con historial de movimientos (auditoría).
- **Compras**: emisión y seguimiento de órdenes de compra a proveedores.
- **ABM**: clientes, proveedores, productos, usuarios y sucursales.
- **Ventas, reportes y dashboard**: facturación, reportes exportables a Excel y gráficos.

### Roles

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Todos los módulos, incluidos Usuarios y Sucursales |
| `GERENTE` | Gestión operativa, compras, productos, ventas, reportes, dashboard y auditoría |
| `VENDEDOR` | Pedidos, operaciones, inventario y clientes |

## Estructura

```
src/
  modelo/        Clases de dominio (Pedido, Producto, Stock_Sucursal, etc.)
  persistencia/  DAOs y conexión JDBC a MySQL/MariaDB (ConexionDB)
  vista/         Pantallas Swing (LoginFrame, MainFrame y paneles de gestión)
  util/          Exportación a Excel y hash de contraseñas (PasswordUtil)
database/
  estructura.sql Esquema de la base de datos
  datos.sql      Datos de ejemplo
db.properties    Conexión a la base (host, puerto, usuario, clave)
pom.xml          Build de Maven
```

## Requisitos

- Java 21 (JDK)
- MySQL o MariaDB
- Maven no hace falta instalarlo: se usa el wrapper `mvnw` incluido, que lo descarga solo.
  Las librerías (`mysql-connector-j` 9.5.0, `flatlaf` 3.0, `jfreechart` 1.5.4) se bajan de Maven Central.

## Cómo ejecutarlo

1. Crear la base de datos importando los scripts:
   ```bash
   mysql -u root < database/estructura.sql
   mysql -u root gestioncomercialdb < database/datos.sql
   ```
2. Revisar `db.properties` (por defecto `root` sin contraseña en `localhost:3306/gestioncomercialdb`).
   La app lo busca en la carpeta desde donde se ejecuta y, si no está, junto al `.jar`.
   Opcional: un `db.local.properties` al lado (no se sube al repo) pisa esos valores y puede indicar
   un servidor local (`db.servidor.exe`, `db.servidor.config`) que la app inicia sola si la base no responde.
3. Compilar y ejecutar:
   ```bash
   mvnw package
   java -jar target/afd-system.jar
   ```
4. Iniciar sesión con un usuario de la tabla `usuarios`.

### Ejecutable para Windows

```bash
mvnw package -Pexe
```

Genera `target/dist/AFD/` con `AFD.exe` y un Java propio incluido: se puede copiar
a otra PC y abrir con doble clic sin instalar Java. La configuración de la base queda en
`AFD/app/db.properties`.

### En un IDE

Importar la carpeta como proyecto Maven (Eclipse: *File > Import > Existing Maven Projects*;
IntelliJ y VS Code lo detectan solos por el `pom.xml`). La clase principal es `vista.LoginFrame`.

## Seguridad

Las contraseñas se guardan con hash PBKDF2-SHA256 con salt (`util.PasswordUtil`). Si en la base
queda alguna contraseña vieja en texto plano, se convierte a hash automáticamente en el primer
login exitoso de ese usuario.
