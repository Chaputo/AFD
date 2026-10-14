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
  util/          Exportación a Excel
database/
  estructura.sql Esquema de la base de datos
  datos.sql      Datos de ejemplo
```

## Requisitos

- Java 21
- MySQL o MariaDB
- Librerías: `mysql-connector-j` 9.5.0, `flatlaf` 3.0, `jfreechart` 1.5.4

## Cómo ejecutarlo

1. Crear la base de datos importando los scripts:
   ```bash
   mysql -u root < database/estructura.sql
   mysql -u root gestioncomercialdb < database/datos.sql
   ```
2. Revisar las credenciales en `src/persistencia/ConexionDB.java` (por defecto `root` sin contraseña en `localhost:3306/gestioncomercialdb`).
3. Agregar los JAR de las librerías al classpath (en Eclipse, ajustar las rutas de `.classpath`).
4. Ejecutar la clase principal `vista.LoginFrame` e iniciar sesión con un usuario de la tabla `usuarios`.
