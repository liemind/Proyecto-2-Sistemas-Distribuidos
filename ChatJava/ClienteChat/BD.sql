CREATE DATABASE estacion;

CREATE TABLE combustible(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   id_comb_empresa INTEGER NOT NULL,
   nombre TEXT NOT NULL,
   costo INTEGER NOT NULL
);

CREATE TABLE transaccion(
   id INTEGER PRIMARY KEY AUTOINCREMENT,
   id_surtidor INTEGER NOT NULL,
   id_combustible INTEGER NOT NULL,
   litros INTEGER NOT NULL,
   costo INTEGER NOT NULL,
   fecha_hora TEXT NOT NULL,
   FOREIGN KEY(id_combustible) REFERENCES combustible(id)
);



/*
* Insert
*/

INSERT INTO combustible (id_comb_empresa, nombre,costo)
VALUES ('93', 1, 10);

INSERT INTO combustible (id_comb_empresa, nombre,costo)
VALUES ('95', 2, 10);

INSERT INTO combustible (id_comb_empresa, nombre,costo)
VALUES ('97', 3, 10);

INSERT INTO combustible (id_comb_empresa, nombre,costo)
VALUES ('Diesel', 4, 10);

INSERT INTO combustible (id_comb_empresa, nombre,costo)
VALUES ('Kerosene', 5, 10);

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

/*Buscar transaccion con fecha anterior a la ultima*/
SELECT * FROM transaccion WHERE  date(fecha_hora) >= date(fechaABuscar);
