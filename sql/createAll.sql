--- ON DELETE CASCADE dla id_spotkania w tabelach GR_W_ROZGR_SPOT i JEDZ_SPOT
ALTER TABLE JEDZ_SPOT
    DROP CONSTRAINT JEDZ_SPOT_SPOTKANIA_FK;

ALTER TABLE JEDZ_SPOT
    ADD CONSTRAINT JEDZ_SPOT_SPOTKANIA_FK
        FOREIGN KEY (ID_SPOTKANIA)
            REFERENCES SPOTKANIA(ID_SPOTKANIA)
                ON DELETE CASCADE;

ALTER TABLE GR_W_ROZGR_SPOT
    DROP CONSTRAINT GR_W_ROZGR_SPOT_SPOTKANIA_FK;

ALTER TABLE GR_W_ROZGR_SPOT
    ADD CONSTRAINT GR_W_ROZGR_SPOT_SPOTKANIA_FK
        FOREIGN KEY (ID_SPOTKANIA)
            REFERENCES SPOTKANIA(ID_SPOTKANIA)
                ON DELETE CASCADE;
---

ALTER TABLE GR_W_ROZGR_SPOT
    DROP CONSTRAINT GR_W_ROZGR_SPOT_ROZGRYWKI_FK;

ALTER TABLE GR_W_ROZGR_SPOT
    ADD CONSTRAINT GR_W_ROZGR_SPOT_ROZGRYWKI_FK
        FOREIGN KEY (ID_ROZGRYWKI)
            REFERENCES ROZGRYWKI (ID_ROZGRYWKI)
                ON DELETE CASCADE;

---

ALTER TABLE rozsz_zest
    DROP CONSTRAINT rozsz_zest_rozszerzenia_FK;
    
ALTER TABLE rozsz_zest
    ADD CONSTRAINT rozsz_zest_rozszerzenia_FK
        FOREIGN KEY (id_roz)
            REFERENCES rozszerzenia(id_roz)
                ON DELETE CASCADE;

---

CREATE TABLE gr_w_rozgr_spot (
    id_gracza    INTEGER NOT NULL,
    id_spotkania INTEGER NOT NULL,
    id_rozgrywki INTEGER NOT NULL,
    pozycja      INTEGER
);

ALTER TABLE gr_w_rozgr_spot
    ADD CONSTRAINT gr_w_rozgr_spot_pk PRIMARY KEY ( id_gracza,
                                                    id_spotkania,
                                                    id_rozgrywki );

CREATE TABLE gracze (
    id_gracza      INTEGER NOT NULL,
    imie           VARCHAR2(32) NOT NULL,
    nazwisko       VARCHAR2(64) NOT NULL,
    data_urodzenia DATE NOT NULL
);

ALTER TABLE gracze ADD CONSTRAINT gracze_pk PRIMARY KEY ( id_gracza );

CREATE TABLE gry (
    id_gry     INTEGER NOT NULL,
    tytul      VARCHAR2(128) NOT NULL,
    min_graczy INTEGER NOT NULL,
    max_graczy INTEGER,
    ocena INTEGER
);

ALTER TABLE gry ADD CONSTRAINT gry_pk PRIMARY KEY ( id_gry );

CREATE TABLE jedz_spot (
    id_spotkania INTEGER NOT NULL,
    nazwa        VARCHAR2(64) NOT NULL
);

ALTER TABLE jedz_spot ADD CONSTRAINT jedz_spot_pk PRIMARY KEY ( id_spotkania,
                                                                nazwa );

CREATE TABLE jedzenie (
    nazwa VARCHAR2(64) NOT NULL
);

ALTER TABLE jedzenie ADD CONSTRAINT jedzenie_pk PRIMARY KEY ( nazwa );

CREATE TABLE miejsca (
    id_miejsca INTEGER NOT NULL,
    adres      VARCHAR2(128) NOT NULL,
    miasto     VARCHAR2(64) NOT NULL
);

ALTER TABLE miejsca ADD CONSTRAINT miejsca_pk PRIMARY KEY ( id_miejsca );

CREATE TABLE rozgrywki (
    id_rozgrywki INTEGER NOT NULL,
    czas         INTEGER NOT NULL,
    id_zestawu   INTEGER NOT NULL
);

ALTER TABLE rozgrywki ADD CONSTRAINT rozgrywki_pk PRIMARY KEY ( id_rozgrywki );

CREATE TABLE rozsz_zest (
    id_roz     INTEGER NOT NULL,
    id_zestawu INTEGER NOT NULL
);

ALTER TABLE rozsz_zest ADD CONSTRAINT rozsz_zest_pk PRIMARY KEY ( id_roz,
                                                                  id_zestawu );

CREATE TABLE rozszerzenia (
    id_roz            INTEGER NOT NULL,
    tytul             VARCHAR2(128) NOT NULL,
    mod_liczby_graczy INTEGER,
    id_gry            INTEGER NOT NULL
);

ALTER TABLE rozszerzenia ADD CONSTRAINT rozszerzenia_pk PRIMARY KEY ( id_roz );

CREATE TABLE spotkania (
    id_spotkania INTEGER NOT NULL,
    data         DATE NOT NULL,
    id_miejsca   INTEGER NOT NULL
);

ALTER TABLE spotkania ADD CONSTRAINT spotkania_pk PRIMARY KEY ( id_spotkania );

CREATE TABLE zestawy (
    id_zestawu INTEGER NOT NULL,
    gry_id_gry INTEGER NOT NULL
);

ALTER TABLE zestawy ADD CONSTRAINT zestawy_pk PRIMARY KEY ( id_zestawu );

ALTER TABLE gr_w_rozgr_spot
    ADD CONSTRAINT gr_w_rozgr_spot_gracze_fk FOREIGN KEY ( id_gracza )
        REFERENCES gracze ( id_gracza );

ALTER TABLE gr_w_rozgr_spot
    ADD CONSTRAINT gr_w_rozgr_spot_rozgrywki_fk FOREIGN KEY ( id_rozgrywki )
        REFERENCES rozgrywki ( id_rozgrywki );

ALTER TABLE gr_w_rozgr_spot
    ADD CONSTRAINT gr_w_rozgr_spot_spotkania_fk FOREIGN KEY ( id_spotkania )
        REFERENCES spotkania ( id_spotkania );

ALTER TABLE jedz_spot
    ADD CONSTRAINT jedz_spot_jedzenie_fk FOREIGN KEY ( nazwa )
        REFERENCES jedzenie ( nazwa );

ALTER TABLE jedz_spot
    ADD CONSTRAINT jedz_spot_spotkania_fk FOREIGN KEY ( id_spotkania )
        REFERENCES spotkania ( id_spotkania );

ALTER TABLE rozgrywki
    ADD CONSTRAINT rozgrywki_zestawy_fk FOREIGN KEY ( id_zestawu )
        REFERENCES zestawy ( id_zestawu );

ALTER TABLE rozsz_zest
    ADD CONSTRAINT rozsz_zest_rozszerzenia_fk FOREIGN KEY ( id_roz )
        REFERENCES rozszerzenia ( id_roz );

ALTER TABLE rozsz_zest
    ADD CONSTRAINT rozsz_zest_zestawy_fk FOREIGN KEY ( id_zestawu )
        REFERENCES zestawy ( id_zestawu );

ALTER TABLE rozszerzenia
    ADD CONSTRAINT rozszerzenia_gry_fk FOREIGN KEY ( id_gry )
        REFERENCES gry ( id_gry );

ALTER TABLE spotkania
    ADD CONSTRAINT spotkania_miejsca_fk FOREIGN KEY ( id_miejsca )
        REFERENCES miejsca ( id_miejsca );

ALTER TABLE zestawy
    ADD CONSTRAINT zestawy_gry_fk FOREIGN KEY ( gry_id_gry )
        REFERENCES gry ( id_gry );

-- liczba graczy musi być większa od 0
ALTER TABLE gry
    ADD CONSTRAINT chk_min_graczy CHECK (min_graczy > 0);

-- maksymalna liczba graczy musi być większa od minimalnej liczby graczy
ALTER TABLE gry
    ADD CONSTRAINT chk_max_graczy CHECK (max_graczy >= min_graczy);
    
-- ocena między 1 a 10
ALTER TABLE gry
    ADD CONSTRAINT chk_ocena CHECK (ocena BETWEEN 1 AND 10);

--zmodyfikowana liczba graczy musi być większa od 0
ALTER TABLE rozszerzenia
    ADD CONSTRAINT chk_mod_liczby_graczy CHECK (mod_liczby_graczy > 0);

-- pozycja gracza w danej grze musi być większa od 0
-- (wygrana to pozycja 1, nie ma ograniczenia na górną wartość z uwagi na specyfikę niektórych gier)
ALTER TABLE gr_w_rozgr_spot
    ADD CONSTRAINT chk_pozycja CHECK (pozycja > 0);

-- sekwencje generujące wartości dla liczbowych kluczy głównych

CREATE SEQUENCE id_gracza_seq
    START WITH 100
    INCREMENT BY 1;
    
CREATE SEQUENCE id_gry_seq
    START WITH 200
    INCREMENT BY 1;

CREATE SEQUENCE id_roz_seq
    START WITH 300
    INCREMENT BY 1;
    
CREATE SEQUENCE id_zestawu_seq
    START WITH 400
    INCREMENT BY 1;
    
CREATE SEQUENCE id_rozgrywki_seq
    START WITH 500
    INCREMENT BY 1;
    
CREATE SEQUENCE id_spotkania_seq
    START WITH 600
    INCREMENT BY 1;

CREATE SEQUENCE id_miejsca_seq
    START WITH 700
    INCREMENT BY 1;

-- procedury wyzwalane uzupełniające klucze generowane sekwencjami

CREATE OR REPLACE TRIGGER UzupelnijIdGracza
BEFORE INSERT ON gracze
FOR EACH ROW
BEGIN
    IF :NEW.id_gracza IS NULL THEN
        :NEW.id_gracza := id_gracza_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdGry
BEFORE INSERT ON gry
FOR EACH ROW
BEGIN
    IF :NEW.id_gry IS NULL THEN
        :NEW.id_gry := id_gry_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdRozszerzenia
BEFORE INSERT ON rozszerzenia
FOR EACH ROW
BEGIN
    IF :NEW.id_roz IS NULL THEN
        :NEW.id_roz := id_roz_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdZestawu
BEFORE INSERT ON zestawy
FOR EACH ROW
BEGIN
    IF :NEW.id_zestawu IS NULL THEN
        :NEW.id_zestawu := id_zestawu_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdRozgrywki
BEFORE INSERT ON rozgrywki
FOR EACH ROW
BEGIN
    IF :NEW.id_rozgrywki IS NULL THEN
        :NEW.id_rozgrywki := id_rozgrywki_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdSpotkania
BEFORE INSERT ON spotkania
FOR EACH ROW
BEGIN
    IF :NEW.id_spotkania IS NULL THEN
        :NEW.id_spotkania := id_spotkania_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER UzupelnijIdMiejsca
BEFORE INSERT ON miejsca
FOR EACH ROW
BEGIN
    IF :NEW.id_miejsca IS NULL THEN
        :NEW.id_miejsca := id_miejsca_seq.NEXTVAL;
    END IF;
END;

CREATE OR REPLACE TRIGGER SprawdzUrodzenieGracza
BEFORE INSERT OR UPDATE ON gracze
FOR EACH ROW
BEGIN
    IF :NEW.data_urodzenia > trunc(sysdate) THEN
         RAISE_APPLICATION_ERROR(-20001, 'Data z przyszlosci');
    END IF;
END;

CREATE OR REPLACE TRIGGER SprawdzDateSpotkania
BEFORE INSERT OR UPDATE ON spotkania
FOR EACH ROW
BEGIN
    IF :NEW.data > trunc(sysdate) THEN
         RAISE_APPLICATION_ERROR(-20001, 'Data z przyszlosci');
    END IF;
END;


-- perspektywa przechowująca id_gracza, id_gry, w którą grał w danej rozgrywce i pozycję, którą wtedy zajął
CREATE OR REPLACE VIEW pozycje_graczy_w_grach AS
    SELECT gr_w_rozgr_spot.id_gracza AS id_gracza, zestawy.gry_id_gry AS id_gry, gr_w_rozgr_spot.pozycja AS pozycja
    FROM gr_w_rozgr_spot JOIN rozgrywki ON gr_w_rozgr_spot.id_rozgrywki = rozgrywki.id_rozgrywki
    JOIN zestawy ON rozgrywki.id_zestawu = zestawy.id_zestawu;

-- funkcja licząca procent wygranych danego gracza w daną grę
CREATE OR REPLACE FUNCTION ProcentWygranych (graczID IN NUMBER, graID IN NUMBER)
    RETURN NUMBER
AS
    wszystkieGry NUMBER;
    wygraneGry NUMBER;
BEGIN
    SELECT COUNT(*) INTO wszystkieGry FROM pozycje_graczy_w_grach
    WHERE id_gracza = graczID AND id_gry = graID;
    SELECT COUNT(*) INTO wygraneGry FROM pozycje_graczy_w_grach
    WHERE id_gracza = graczID AND id_gry = graID AND pozycja = 1;
    RETURN ROUND((wygraneGry / wszystkieGry)*100, 2);
END ProcentWygranych;

-- procedura towrząca zestaw na podstawie podanego id rozszerzenia (szuka id gry).
CREATE OR REPLACE PROCEDURE DodajZestawGraRozszerzenie
    (pIdRoz IN NUMBER)
    IS
    vIdZest NUMBER := id_zestawu_seq.NEXTVAL;
BEGIN
    dbms_output.put_line(vIdZest);
    INSERT INTO zestawy(id_zestawu, gry_id_gry)
    VALUES(vIdZest, (SELECT id_gry FROM rozszerzenia WHERE id_roz = pIdRoz));
    
    INSERT INTO rozsz_zest VALUES(pIdRoz, vIdZest);
END DodajZestawGraRozszerzenie;

-- procedura dodająca spotkanie i miejsce jednocześnie
CREATE OR REPLACE PROCEDURE DodajSpotkanieNoweMiejsce
(
    vAdres IN miejsca.adres%TYPE,
    vMiasto IN miejsca.miasto%TYPE,
    vData IN spotkania.data%TYPE
)
IS
    id miejsca.id_miejsca%TYPE;
BEGIN
  INSERT INTO miejsca(adres, miasto)
  VALUES(vAdres, vMiasto)
  RETURNING id_miejsca INTO id;

  INSERT INTO spotkania(data, id_miejsca)
  VALUES(vData, id);

END;

-- indeks usprawniajacy wyszukanie gier po tytule
CREATE INDEX gry_tytul_idx ON gry(tytul);

-- indeks usprawniajacy wyszukanie rozszerzen po id_gry
CREATE INDEX rozszerzenia_id_gry_idx ON rozszerzenia(id_gry);

-- indeks usprawniajacy filtrowanie rozgrywek pod względem zajętego miejsca
CREATE BITMAP INDEX pozycja_gr_w_rozgr_spot_idx ON gr_w_rozgr_spot(pozycja);
