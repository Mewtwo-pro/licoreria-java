CREATE DATABASE IF NOT EXISTS licoreria_dcorleone;
USE licoreria_dcorleone;

-- ========================================================
-- ESTRUCTURA DE TABLAS (CORREGIDA)
-- ========================================================

-- 1. TABLA: ROLES
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- 2. TABLA: USUARIOS / EMPLEADOS
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(150) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    turno ENUM('Mañana', 'Tarde', 'Noche') NOT NULL DEFAULT 'Mañana', -- <-- CORREGIDO: Tenía un ";" y ahora tiene ","
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol)
);

-- 3. TABLA: PROVEEDORES
CREATE TABLE proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_proveedor VARCHAR(150) NOT NULL,
    ruc VARCHAR(11) UNIQUE,
    contacto VARCHAR(100),
    telefono VARCHAR(20),
    email VARCHAR(100),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. TABLA: CATEGORIAS
CREATE TABLE categorias (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE
);

-- 5. TABLA: PRODUCTOS (OPTIMIZADA PARA TU INTERFAZ)
CREATE TABLE productos (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,                 -- <-- AGREGADO: Para "Código SKU" (ej. 'WBL001')
    nombre_producto VARCHAR(150) NOT NULL,
    id_categoria INT NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0,
    ubicacion_almacen VARCHAR(100) DEFAULT 'General',-- <-- AGREGADO: Para "Ubicación Almacén" (ej. 'Estante A3')
    stock_critico INT NOT NULL DEFAULT 10,           -- <-- AGREGADO: Para "Stock Crítico" (ej. '15')
    precio_venta DECIMAL(10,2) NOT NULL,
    ultimo_precio_compra DECIMAL(10,2),              -- <-- RENOMBRADO: Para "Último Precio de Compra"
    id_proveedor_principal INT,                      -- <-- Para "Último Proveedor"
    imagen_url VARCHAR(255) DEFAULT '/images/puntoVenta/whisky.png',
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria),
    FOREIGN KEY (id_proveedor_principal) REFERENCES proveedores(id_proveedor)
);
-- 6. TABLA: VENTAS / ATENCIONES
CREATE TABLE ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tipo_pago ENUM('Efectivo', 'Tarjeta') NOT NULL,
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    id_usuario_cajero INT NOT NULL,
    FOREIGN KEY (id_usuario_cajero) REFERENCES usuarios(id_usuario)
);

-- 7. TABLA: DETALLE DE VENTAS
CREATE TABLE detalle_ventas (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- 8. TABLA: REPARTIDORES / MOTOS
CREATE TABLE repartidores (
    id_repartidor INT AUTO_INCREMENT PRIMARY KEY,
    nombre_repartidor VARCHAR(150) NOT NULL,
    vehiculo VARCHAR(50) NOT NULL,
    estado ENUM('Libre', 'En Ruta') DEFAULT 'Libre'
);

-- 9. TABLA: DELIVERIES
CREATE TABLE deliveries (
    id_delivery INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL UNIQUE,
    cliente_nombre VARCHAR(150) NOT NULL,
    cliente_telefono VARCHAR(20) NOT NULL,
    direccion_envio VARCHAR(255) NOT NULL,
    referencia VARCHAR(255),
    estado_delivery ENUM('En Preparación', 'En Camino', 'Entregado') DEFAULT 'En Preparación',
    id_repartidor INT NULL,
    tiempo_promedio_min INT DEFAULT 28,
    fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
    FOREIGN KEY (id_repartidor) REFERENCES repartidores(id_repartidor)
);

-- 10. TABLA: ORDENES DE COMPRA
CREATE TABLE ordenes_compra (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor INT NOT NULL,
    monto_total DECIMAL(10,2) NOT NULL,
    estado ENUM('Pendiente', 'Recibido') DEFAULT 'Pendiente',
    fecha_orden TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor)
);


-- ========================================================
-- INSERCIÓN DE 6 DATOS POR TABLA
-- ========================================================

-- 1. Roles
INSERT INTO roles (nombre_rol) VALUES 
('Admin'), ('Cajero'), ('Mesero'), ('Repartidor'), ('Almacenero'), ('Supervisor');

-- 2. Usuarios
INSERT INTO usuarios (nombre_completo, username, password, id_rol, activo, turno) VALUES 
('Manuel', 'admin', 'admin', 1, TRUE, 'Mañana'),
('Carlos Mendoza', 'carlos.admin@licoreria.com', 'admin123', 1, TRUE, 'Mañana'),
('Ana Gomez', 'ana.caja@licoreria.com', 'caja2026', 2, TRUE, 'Tarde'),
('Juan Quispe', 'juan.mesero@licoreria.com', 'juanito99', 3, TRUE, 'Noche'),
('Luis Torres', 'luis.moto@licoreria.com', 'moto2rep', 4, TRUE, 'Tarde'),
('Sofia Castro', 'sofia.almacen@licoreria.com', 'sofia_almacen', 5, TRUE, 'Mañana'),
('Jorge Rivas', 'jorge.moto@licoreria.com', 'moto1rep', 4, TRUE, 'Noche');

-- 3. Proveedores
INSERT INTO proveedores (nombre_proveedor, ruc, contacto, telefono, email) VALUES 
('Cervecería Nacional S.A.', '20123456789', 'Pedro Infante', '987654321', 'ventas@cervecerianacional.pe'),
('Distribuidora El Ronero', '20987654321', 'María Delgado', '912345678', 'contacto@elronero.com'),
('Importaciones del Whisky SAC', '20555666777', 'Roberto Silva', '933445566', 'rsilva@importwhisky.pe'),
('Bodegas Vinícolas del Sur', '20444333222', 'Elena Flores', '955667788', 'pedidos@vinosdelsur.com'),
('Hielos y Complementos Polar', '20888999111', 'Carlos Ruíz', '922883377', 'ventas@hielospolar.com'),
('Licores Mundiales Premium', '20777666555', 'Andrés Castro', '944112233', 'acastro@licorespremium.pe');

-- 4. Categorías
INSERT INTO categorias (nombre_categoria) VALUES 
('Whisky'), ('Cerveza'), ('Ron'), ('Vino'), ('Tequila'), ('Complementos');

-- 5. Productos
INSERT INTO productos (
    sku, 
    nombre_producto, 
    id_categoria, 
    stock_actual, 
    ubicacion_almacen, 
    stock_critico, 
    precio_venta, 
    ultimo_precio_compra, 
    id_proveedor_principal, 
    imagen_url
) VALUES 
('WBL001', 'Whisky Black Label 750ml', 1, 24, 'Estante A3', 15, 95.00, 45.00, 3, '/images/puntoVenta/black_label.png'),
('CCU004', 'Cerveza Pilsen Callao 620ml', 2, 120, 'Estante C1', 25, 8.50, 4.20, 1, '/images/puntoVenta/pilsen.png'),
('RCT012', 'Ron Cartavio Selecto 750ml', 3, 50, 'Estante B2', 10, 45.00, 20.00, 2, '/images/puntoVenta/whisky.png'),
('VIN001', 'Vino Tinto Intipalka Malbec', 4, 30, 'Estante D4', 8, 38.00, 18.50, 4, '/images/puntoVenta/intipalka.png'),
('TEQ001', 'Tequila José Cuervo Especial', 5, 15, 'Estante A2', 5, 65.00, 32.00, 6, '/images/puntoVenta/jose_cuervo.png'),
('HIE001', 'Bolsa de Hielo Gourmet 3Kg', 6, 40, 'Congelador 1', 12, 7.00, 2.50, 5, '/images/puntoVenta/hielo.png');
-- 6. Ventas
INSERT INTO ventas (tipo_pago, total, id_usuario_cajero) VALUES 
('Efectivo', 103.50, 2),
('Tarjeta', 45.00, 2),
('Efectivo', 72.00, 2),
('Tarjeta', 133.00, 2),
('Tarjeta', 95.00, 2),
('Efectivo', 15.50, 2);

-- 7. Detalle de Ventas
INSERT INTO detalle_ventas (id_venta, id_producto, cantidad, precio_unitario) VALUES 
(1, 1, 1, 95.00),
(1, 2, 1, 8.50),
(2, 3, 1, 45.00),
(3, 4, 1, 38.00),
(4, 3, 2, 45.00),
(5, 1, 1, 95.00);

-- 8. Repartidores
INSERT INTO repartidores (nombre_repartidor, vehiculo, estado) VALUES 
('Jorge Rivas', 'Moto 1', 'Libre'),
('Luis Torres', 'Moto 2', 'En Ruta'),
('Carlos Flores', 'Moto 3', 'Libre'),
('Miguel Benites', 'Moto 4', 'Libre'),
('Roberto Gomez', 'Moto 5', 'En Ruta'),
('Fernando Diaz', 'Moto 6', 'Libre');

-- 9. Deliveries
-- (Se agregan 3 ventas adicionales exclusivas para cumplir la restricción UNIQUE del delivery)
INSERT INTO ventas (tipo_pago, total, id_usuario_cajero) VALUES ('Tarjeta', 65.00, 2), ('Efectivo', 38.00, 2), ('Tarjeta', 7.00, 2);

INSERT INTO deliveries (id_venta, cliente_nombre, cliente_telefono, direccion_envio, referencia, estado_delivery, id_repartidor, tiempo_promedio_min) VALUES 
(2, 'Marcos Perez', '988223344', 'Av. Larco 456, Miraflores', 'Frente al Parque Kennedy', 'Entregado', 1, 25),
(3, 'Lucia Fernandez', '955112233', 'Calle Los Pinos 123, San Isidro', 'Cerca a la clínica El Golf', 'En Camino', 2, 28),
(5, 'Ricardo Gareca', '966445566', 'Av. Javier Prado Este 2540', 'Al costado del Jockey Plaza', 'En Preparación', NULL, 35),
(7, 'Alejandra Ruiz', '911778899', 'Jr. Carabaya 580, Centro de Lima', 'A media cuadra de la Plaza Mayor', 'Entregado', 4, 22),
(8, 'David Choque', '933002211', 'Av. El Sol 789, Barranco', 'Frente a la estación del Metropolitano', 'En Camino', 5, 30),
(9, 'Patricia Luna', '922448855', 'Av. Brasil 1420, Pueblo Libre', 'Esquina con Jr. Vivanco', 'En Preparación', NULL, 28);

-- 10. Órdenes de Compra
INSERT INTO ordenes_compra (id_proveedor, monto_total, estado) VALUES 
(1, 1200.00, 'Recibido'),
(3, 3500.00, 'Pendiente'),
(2, 850.00, 'Recibido'),
(4, 1500.00, 'Pendiente'),
(5, 300.00, 'Recibido'),
(6, 2400.00, 'Pendiente');
