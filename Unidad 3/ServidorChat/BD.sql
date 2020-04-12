CREATE DATABASE empresa;

CREATE TABLE combustible(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   nombre TEXT NOT NULL,
   costo INTEGER NOT NULL,
   fecha_hora TEXT NOT NULL
);

CREATE TABLE estacion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   nombre TEXT NOT NULL
);

CREATE TABLE transaccion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   id_estacion INTEGER NOT NULL,
   id_surtidor INTEGER NOT NULL,
   id_combustible INTEGER NOT NULL,
   litros INTEGER NOT NULL,
   costo INTEGER NOT NULL,
   fecha_hora TEXT NOT NULL,
   FOREIGN KEY(id_estacion) REFERENCES estacion(id),
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