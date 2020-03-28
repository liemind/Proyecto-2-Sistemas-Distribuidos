CREATE DATABASE empresa;

CREATE TABLE combustible(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   nombre TEXT NOT NULL,
   costo INTEGER NOT NULL
);

CREATE TABLE estacion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   nombre TEXT NOT NULL
);

CREATE TABLE surtidor(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   id_estacion INTEGER NOT NULL,
   id_numero_surtidor INTEGER NOT NULL,
   transacciones INTEGER,
   FOREIGN KEY(id_estacion) REFERENCES estacion(id)
);

CREATE TABLE transaccion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   id_transaccion_estacion INTEGER NOT NULL,
   id_surtidor INTEGER NOT NULL,
   id_combustible INTEGER NOT NULL,
   litros INTEGER NOT NULL,
   costo INTEGER NOT NULL,
   FOREIGN KEY(id_surtidor) REFERENCES surtidor(id),
   FOREIGN KEY(id_combustible) REFERENCES combustible(id)
);

/*
* Insert
*/

INSERT INTO combustible (nombre,costo)
VALUES ('93', 10);

INSERT INTO combustible (nombre,costo)
VALUES ('95', 10);

INSERT INTO combustible (nombre,costo)
VALUES ('97', 10);

INSERT INTO combustible (nombre,costo)
VALUES ('Diesel', 10);

INSERT INTO combustible (nombre,costo)
VALUES ('Kerosene', 10);

/*Upgradear Combustible*/
