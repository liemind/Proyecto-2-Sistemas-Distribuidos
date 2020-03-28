CREATE DATABASE estacion;

CREATE TABLE combustible(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   nombre TEXT NOT NULL,
   costo INTEGER NOT NULL
);

CREATE TABLE surtidor(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   transacciones INTEGER
);

CREATE TABLE transaccion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
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

INSERT INTO surtidor(transacciones)
VALUES(0);
INSERT INTO surtidor(transacciones)
VALUES(0);
INSERT INTO surtidor(transacciones)
VALUES(0);
INSERT INTO surtidor(transacciones)
VALUES(0);

/*Consultar combustibles*/
SELECT nombre,costo FROM combustible;

/*Consultar surtidor*/
SELECT id,transacciones FROM surtidor WHERE id = 1;

/*Upgradear surtidor*/
UPDATE surtidor SET transacciones = numero WHERE id = 1;

/*Crear transaccion*/
INSERT INTO transaccion (id_surtidor, id_combustible, litros, costo)
VALUES(x,x,x,x);
