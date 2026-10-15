-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: gestioncomercialdb
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `gestioncomercialdb`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `gestioncomercialdb` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;

USE `gestioncomercialdb`;

--
-- Table structure for table `cliente`
--

DROP TABLE IF EXISTS `cliente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cliente` (
  `id_cliente` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id_cliente`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `inventario_sucursal`
--

DROP TABLE IF EXISTS `inventario_sucursal`;
/*!50001 DROP VIEW IF EXISTS `inventario_sucursal`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
/*!50001 CREATE VIEW `inventario_sucursal` AS SELECT
 1 AS `id`,
  1 AS `sku_producto`,
  1 AS `id_sucursal`,
  1 AS `stock`,
  1 AS `cantidad`,
  1 AS `fecha_ultimo_movimiento` */;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `linea_oc`
--

DROP TABLE IF EXISTS `linea_oc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `linea_oc` (
  `id_linea` int(11) NOT NULL AUTO_INCREMENT,
  `id_oc` int(11) NOT NULL,
  `sku_producto` varchar(20) DEFAULT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_compra_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  `id_proveedor` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_linea`),
  KEY `id_oc` (`id_oc`),
  KEY `linea_oc_ibfk_2` (`sku_producto`),
  KEY `linea_oc_ibfk_3` (`id_proveedor`),
  CONSTRAINT `linea_oc_ibfk_1` FOREIGN KEY (`id_oc`) REFERENCES `orden_compra` (`id_oc`),
  CONSTRAINT `linea_oc_ibfk_2` FOREIGN KEY (`sku_producto`) REFERENCES `producto` (`sku`),
  CONSTRAINT `linea_oc_ibfk_3` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `movimiento_stock`
--

DROP TABLE IF EXISTS `movimiento_stock`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `movimiento_stock` (
  `id_movimiento` int(11) NOT NULL AUTO_INCREMENT,
  `fecha` datetime NOT NULL,
  `tipo` varchar(10) NOT NULL,
  `cantidad` int(11) NOT NULL,
  `sku_producto` varchar(20) DEFAULT NULL,
  `id_sucursal` int(11) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `stock_resultante` int(11) DEFAULT NULL,
  `id_oc_relacionada` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_movimiento`),
  KEY `id_oc_relacionada` (`id_oc_relacionada`),
  KEY `movimiento_stock_ibfk_1` (`sku_producto`),
  CONSTRAINT `movimiento_stock_ibfk_1` FOREIGN KEY (`sku_producto`) REFERENCES `producto` (`sku`),
  CONSTRAINT `movimiento_stock_ibfk_2` FOREIGN KEY (`id_oc_relacionada`) REFERENCES `orden_compra` (`id_oc`)
) ENGINE=InnoDB AUTO_INCREMENT=119 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orden_compra`
--

DROP TABLE IF EXISTS `orden_compra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orden_compra` (
  `id_oc` int(11) NOT NULL AUTO_INCREMENT,
  `id_proveedor` int(11) NOT NULL,
  `id_usuario` int(11) NOT NULL,
  `fecha_emision` datetime NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `estado` varchar(50) NOT NULL,
  PRIMARY KEY (`id_oc`),
  KEY `id_proveedor` (`id_proveedor`),
  KEY `id_usuario` (`id_usuario`),
  CONSTRAINT `orden_compra_ibfk_1` FOREIGN KEY (`id_proveedor`) REFERENCES `proveedor` (`id_proveedor`),
  CONSTRAINT `orden_compra_ibfk_2` FOREIGN KEY (`id_usuario`) REFERENCES `usuarios` (`id_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedido_detalle`
--

DROP TABLE IF EXISTS `pedido_detalle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pedido_detalle` (
  `id_detalle` int(11) NOT NULL AUTO_INCREMENT,
  `id_pedido` int(11) NOT NULL,
  `sku_producto` varchar(100) DEFAULT NULL,
  `cantidad` int(11) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id_detalle`),
  KEY `fk_pedido_detalle` (`id_pedido`),
  CONSTRAINT `fk_pedido_detalle` FOREIGN KEY (`id_pedido`) REFERENCES `pedidos` (`id_pedido`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=428 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pedidos`
--

DROP TABLE IF EXISTS `pedidos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pedidos` (
  `id_pedido` int(11) NOT NULL AUTO_INCREMENT,
  `fecha_pedido` datetime DEFAULT current_timestamp(),
  `total_pedido` decimal(10,2) NOT NULL,
  `id_cliente` int(11) DEFAULT NULL,
  `id_usuario` int(11) DEFAULT NULL,
  `id_sucursal` int(11) DEFAULT NULL,
  `estado_entrega` varchar(50) DEFAULT 'Pendiente',
  `tipo_pago` varchar(50) DEFAULT 'Efectivo',
  PRIMARY KEY (`id_pedido`)
) ENGINE=InnoDB AUTO_INCREMENT=184 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `producto`
--

DROP TABLE IF EXISTS `producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `producto` (
  `sku` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `stockMinimo` int(11) DEFAULT NULL,
  `unidadMedida` varchar(20) DEFAULT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  PRIMARY KEY (`sku`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `proveedor`
--

DROP TABLE IF EXISTS `proveedor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `proveedor` (
  `id_proveedor` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `cuit` varchar(20) NOT NULL,
  `contacto` varchar(255) DEFAULT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_proveedor`),
  UNIQUE KEY `cuit` (`cuit`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_sucursal`
--

DROP TABLE IF EXISTS `stock_sucursal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `stock_sucursal` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sku_producto` varchar(20) DEFAULT NULL,
  `id_sucursal` int(11) NOT NULL,
  `cantidad_disponible` int(11) NOT NULL,
  `fecha_ultimo_movimiento` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_producto_sucursal` (`sku_producto`,`id_sucursal`)
) ENGINE=InnoDB AUTO_INCREMENT=261 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sucursales`
--

DROP TABLE IF EXISTS `sucursales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sucursales` (
  `id_sucursal` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `direccion` varchar(255) DEFAULT NULL,
  `activa` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id_sucursal`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuarios`
--

DROP TABLE IF EXISTS `usuarios`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usuarios` (
  `id_usuario` int(11) NOT NULL AUTO_INCREMENT,
  `nombre_completo` varchar(100) DEFAULT NULL,
  `nombre_usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` varchar(50) NOT NULL,
  `activo` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `nombre_usuario` (`nombre_usuario`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping events for database 'gestioncomercialdb'
--

--
-- Dumping routines for database 'gestioncomercialdb'
--

--
-- Current Database: `gestioncomercialdb`
--

USE `gestioncomercialdb`;

--
-- Final view structure for view `inventario_sucursal`
--

/*!50001 DROP VIEW IF EXISTS `inventario_sucursal`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_unicode_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `inventario_sucursal` AS select `stock_sucursal`.`id` AS `id`,`stock_sucursal`.`sku_producto` AS `sku_producto`,`stock_sucursal`.`id_sucursal` AS `id_sucursal`,`stock_sucursal`.`cantidad_disponible` AS `stock`,`stock_sucursal`.`cantidad_disponible` AS `cantidad`,`stock_sucursal`.`fecha_ultimo_movimiento` AS `fecha_ultimo_movimiento` from `stock_sucursal` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-13 23:32:18
