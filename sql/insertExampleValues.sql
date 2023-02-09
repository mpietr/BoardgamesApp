INSERT INTO gracze(imie, nazwisko, data_urodzenia) VALUES('JAN', 'KOWALSKI', TO_DATE('20-12-1999', 'dd-mm-yyyy'));
INSERT INTO gracze(imie, nazwisko, data_urodzenia) VALUES('JANINA', 'KOWALSKA', TO_DATE('12-12-2002', 'dd-mm-yyyy'));

INSERT INTO gracze(imie, nazwisko, data_urodzenia) VALUES('PAN', 'ERROR', TO_DATE('20-12-2030', 'dd-mm-yyyy')); -- DZIALA SPRAWDZANIE DATY

INSERT INTO gry(tytul, min_graczy) VALUES('PANSTWA-MIASTA', 2);
INSERT INTO gry(tytul, min_graczy, max_graczy) VALUES('TERRAFORMACJA MARSA', 1, 5);

INSERT INTO rozszerzenia(tytul, id_gry) VALUES(
'PRELUDIUM',
(SELECT id_gry FROM gry WHERE tytul LIKE 'TERRAFORMACJA MARSA')
);

INSERT INTO miejsca(adres, miasto) VALUES('UL. ZMYSLONA 1', 'POZNAN');

INSERT INTO spotkania(data, id_miejsca) VALUES(
TO_DATE('12-11-2022', 'dd-mm-yyyy'),
(SELECT id_miejsca FROM miejsca WHERE adres LIKE 'UL. ZMYSLONA 1')
);


INSERT INTO zestawy(gry_id_gry) VALUES (
(SELECT id_gry FROM gry WHERE tytul LIKE 'PANSTWA-MIASTA')
);

INSERT INTO zestawy(gry_id_gry) VALUES(
(SELECT id_gry FROM gry WHERE tytul LIKE 'TERRAFORMACJA MARSA')
);

SELECT * FROM gracze;
select * from spotkania;
select * from rozgrywki;
SELECT * FROM gry;
SELECT * FROM zestawy;
SELECT * FROM rozszerzenia;

INSERT INTO rozsz_zest VALUES(300, 402);

INSERT INTO rozgrywki(czas, id_zestawu) VALUES(90, 402);
INSERT INTO rozgrywki(czas, id_zestawu) VALUES(90, 400);

INSERT INTO rozgrywki(czas, id_zestawu) VALUES(70, 402);
INSERT INTO rozgrywki(czas, id_zestawu) VALUES(110, 402);

INSERT INTO gr_w_rozgr_spot VALUES(100, 600, 500, 1);
INSERT INTO gr_w_rozgr_spot VALUES(101, 600, 500, 2);

INSERT INTO gr_w_rozgr_spot VALUES(100, 600, 501, 1);
INSERT INTO gr_w_rozgr_spot VALUES(101, 600, 501, 1);

INSERT INTO gr_w_rozgr_spot VALUES(100, 600, 502, 1);
INSERT INTO gr_w_rozgr_spot VALUES(101, 600, 502, 2);

INSERT INTO gr_w_rozgr_spot VALUES(100, 600, 503, 2);
INSERT INTO gr_w_rozgr_spot VALUES(101, 600, 503, 1);

select * from gr_w_rozgr_spot order by id_rozgrywki, id_gracza;

SELECT ProcentWygranych(101, 201) FROM dual;