/* ============================================================================
   ROTA DO FOLEGO - Global Solution 2026/2 - CitizenTech - FIAP
   Disciplina : Building Relational Database (Oracle)
   Integrantes: <NOME 1> RM<00000> | <NOME 2> RM<00000> | <NOME 3> RM<00000>
   Turma      : 1TDS - Fevereiro

   Ordem do arquivo (conforme edital):
     1) Tabelas (+ sequences usadas pela API Java)
     2) 60 INSERTs
     3) 10 consultas numeradas
     4) UPDATE e DELETE (ambos com WHERE)

   Como executar no SQL Developer: abrir este arquivo no SQL Worksheet,
   conectar no Oracle da FIAP e usar "Executar Script" (F5).
   ODS atendidos: 11 (Cidades sustentaveis), 3 (Saude e bem-estar) e
                  10 (Reducao das desigualdades).
   Obs.: dados sem acento de proposito (evita problema de codificacao).
   ============================================================================ */

-- ----------------------------------------------------------------------------
-- (Opcional) Limpeza para executar o script de novo. Descomente se precisar.
-- ----------------------------------------------------------------------------
-- DROP TABLE tb_trajeto_ponto  CASCADE CONSTRAINTS;
-- DROP TABLE tb_avaliacao      CASCADE CONSTRAINTS;
-- DROP TABLE tb_trajeto        CASCADE CONSTRAINTS;
-- DROP TABLE tb_ponto_descanso CASCADE CONSTRAINTS;
-- DROP TABLE tb_cidadao        CASCADE CONSTRAINTS;
-- DROP TABLE tb_tipo_ponto     CASCADE CONSTRAINTS;
-- DROP SEQUENCE sq_tb_tipo_ponto;
-- DROP SEQUENCE sq_tb_cidadao;
-- DROP SEQUENCE sq_tb_ponto_descanso;
-- DROP SEQUENCE sq_tb_trajeto;
-- DROP SEQUENCE sq_tb_avaliacao;


/* ============================================================================
   1) CREATE TABLE  (na ordem das chaves estrangeiras)
   ============================================================================ */

-- tb_tipo_ponto: categorias de ponto de descanso (banco, sombra, marquise...).
-- Lado "1" do relacionamento TipoPonto 1:N PontoDescanso.
CREATE TABLE tb_tipo_ponto (
    cd_tipo   NUMBER(10)   NOT NULL,
    nm_tipo   VARCHAR2(30) NOT NULL,
    CONSTRAINT pk_tb_tipo_ponto PRIMARY KEY (cd_tipo),
    CONSTRAINT uq_tb_tipo_ponto_nm UNIQUE (nm_tipo)
);

-- tb_cidadao: morador que usa e colabora com o app (idoso, gestante, PcD, cardiaco).
-- Lado "1" do relacionamento Cidadao 1:N Trajeto e Cidadao 1:N Avaliacao.
CREATE TABLE tb_cidadao (
    cd_cidadao  NUMBER(10)   NOT NULL,
    nm_cidadao  VARCHAR2(80) NOT NULL,
    ds_email    VARCHAR2(80) NOT NULL,
    nr_idade    NUMBER(3)    NOT NULL,
    ds_perfil   VARCHAR2(20) NOT NULL,
    CONSTRAINT pk_tb_cidadao PRIMARY KEY (cd_cidadao),
    CONSTRAINT uq_tb_cidadao_email UNIQUE (ds_email),
    CONSTRAINT ck_tb_cidadao_idade CHECK (nr_idade BETWEEN 0 AND 120),
    CONSTRAINT ck_tb_cidadao_perfil CHECK (ds_perfil IN ('idoso','gestante','pcd','cardiaco'))
);

-- tb_ponto_descanso: local onde da para descansar durante o trajeto a pe.
-- Lado "N" de TipoPonto 1:N PontoDescanso; lado "1" de PontoDescanso 1:N Avaliacao.
CREATE TABLE tb_ponto_descanso (
    cd_ponto      NUMBER(10)    NOT NULL,
    nm_ponto      VARCHAR2(80)  NOT NULL,
    ds_endereco   VARCHAR2(120) NOT NULL,
    vl_latitude   NUMBER(9,6),
    vl_longitude  NUMBER(9,6),
    st_coberto    CHAR(1)       DEFAULT 'N' NOT NULL,
    dt_cadastro   DATE          DEFAULT SYSDATE NOT NULL,
    cd_tipo       NUMBER(10)    NOT NULL,
    CONSTRAINT pk_tb_ponto_descanso PRIMARY KEY (cd_ponto),
    CONSTRAINT fk_ponto_tipo FOREIGN KEY (cd_tipo) REFERENCES tb_tipo_ponto (cd_tipo),
    CONSTRAINT ck_ponto_coberto CHECK (st_coberto IN ('S','N')),
    CONSTRAINT ck_ponto_latitude CHECK (vl_latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_ponto_longitude CHECK (vl_longitude BETWEEN -180 AND 180)
);

-- tb_trajeto: caminho a pe planejado por um cidadao (origem, destino, distancia).
-- Lado "N" de Cidadao 1:N Trajeto.
CREATE TABLE tb_trajeto (
    cd_trajeto      NUMBER(10)    NOT NULL,
    ds_origem       VARCHAR2(120) NOT NULL,
    ds_destino      VARCHAR2(120) NOT NULL,
    vl_distancia_m  NUMBER(8)     NOT NULL,
    dt_trajeto      DATE          DEFAULT SYSDATE NOT NULL,
    cd_cidadao      NUMBER(10)    NOT NULL,
    CONSTRAINT pk_tb_trajeto PRIMARY KEY (cd_trajeto),
    CONSTRAINT fk_trajeto_cidadao FOREIGN KEY (cd_cidadao) REFERENCES tb_cidadao (cd_cidadao),
    CONSTRAINT ck_trajeto_distancia CHECK (vl_distancia_m > 0)
);

-- tb_avaliacao: nota (1 a 5) que um cidadao da a um ponto de descanso.
-- Regra de negocio: o mesmo cidadao nao avalia o mesmo ponto duas vezes (UNIQUE).
CREATE TABLE tb_avaliacao (
    cd_avaliacao   NUMBER(10)    NOT NULL,
    nr_nota        NUMBER(1)     NOT NULL,
    ds_comentario  VARCHAR2(200),
    dt_avaliacao   DATE          DEFAULT SYSDATE NOT NULL,
    cd_ponto       NUMBER(10)    NOT NULL,
    cd_cidadao     NUMBER(10)    NOT NULL,
    CONSTRAINT pk_tb_avaliacao PRIMARY KEY (cd_avaliacao),
    CONSTRAINT fk_avaliacao_ponto FOREIGN KEY (cd_ponto) REFERENCES tb_ponto_descanso (cd_ponto),
    CONSTRAINT fk_avaliacao_cidadao FOREIGN KEY (cd_cidadao) REFERENCES tb_cidadao (cd_cidadao),
    CONSTRAINT ck_avaliacao_nota CHECK (nr_nota BETWEEN 1 AND 5),
    CONSTRAINT uq_avaliacao_cidadao_ponto UNIQUE (cd_cidadao, cd_ponto)
);

-- tb_trajeto_ponto: tabela associativa do N:N Trajeto x PontoDescanso
-- (quais pontos de descanso entram em cada trajeto e em que ordem).
CREATE TABLE tb_trajeto_ponto (
    cd_trajeto  NUMBER(10) NOT NULL,
    cd_ponto    NUMBER(10) NOT NULL,
    nr_ordem    NUMBER(2)  NOT NULL,
    CONSTRAINT pk_tb_trajeto_ponto PRIMARY KEY (cd_trajeto, cd_ponto),
    CONSTRAINT fk_trajponto_trajeto FOREIGN KEY (cd_trajeto) REFERENCES tb_trajeto (cd_trajeto),
    CONSTRAINT fk_trajponto_ponto FOREIGN KEY (cd_ponto) REFERENCES tb_ponto_descanso (cd_ponto),
    CONSTRAINT ck_trajponto_ordem CHECK (nr_ordem > 0)
);

-- Comentarios gravados no dicionario de dados do Oracle
COMMENT ON TABLE tb_tipo_ponto     IS 'Categorias de ponto de descanso: banco, sombra, marquise, bebedouro, banheiro acessivel';
COMMENT ON TABLE tb_cidadao        IS 'Moradores que usam e colaboram com o app: idoso, gestante, pcd, cardiaco';
COMMENT ON TABLE tb_ponto_descanso IS 'Locais onde e possivel descansar durante um trajeto a pe';
COMMENT ON TABLE tb_trajeto        IS 'Caminhos a pe planejados pelos cidadaos';
COMMENT ON TABLE tb_avaliacao      IS 'Notas de 1 a 5 dadas pelos cidadaos aos pontos de descanso';
COMMENT ON TABLE tb_trajeto_ponto  IS 'Tabela associativa N:N entre trajeto e ponto de descanso';

-- Sequences (usadas pela API Java do Rota do Folego).
-- Comecam em 100 porque os INSERTs abaixo usam os codigos de 1 a 18.
CREATE SEQUENCE sq_tb_tipo_ponto     START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_cidadao        START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_ponto_descanso START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_trajeto        START WITH 100 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE sq_tb_avaliacao      START WITH 100 INCREMENT BY 1 NOCACHE;


/* ============================================================================
   2) INSERTs  -  60 registros no total
      tb_tipo_ponto 5 | tb_cidadao 12 | tb_ponto_descanso 16 |
      tb_trajeto 8 | tb_avaliacao 10 | tb_trajeto_ponto 9
   ============================================================================ */

-- tb_tipo_ponto (5)
INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (1, 'banco');
INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (2, 'sombra');
INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (3, 'marquise');
INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (4, 'bebedouro');
INSERT INTO tb_tipo_ponto (cd_tipo, nm_tipo) VALUES (5, 'banheiro acessivel');

-- tb_cidadao (12)
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (1,  'Maria Aparecida Souza',  'maria.souza@email.com',    72, 'idoso');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (2,  'Jose Carlos Ferreira',  'jose.ferreira@email.com',  68, 'idoso');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (3,  'Ana Paula Lima',        'ana.lima@email.com',       29, 'gestante');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (4,  'Roberto Alves',         'roberto.alves@email.com',  55, 'cardiaco');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (5,  'Luciana Martins',       'luciana.martins@email.com',34, 'gestante');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (6,  'Carlos Eduardo Santos', 'carlos.santos@email.com',  41, 'pcd');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (7,  'Helena Barbosa',        'helena.barbosa@email.com', 79, 'idoso');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (8,  'Fernanda Rocha',        'fernanda.rocha@email.com', 31, 'gestante');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (9,  'Antonio Pereira',       'antonio.pereira@email.com',63, 'cardiaco');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (10, 'Sandra Oliveira',       'sandra.oliveira@email.com',47, 'pcd');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (11, 'Joao Batista Costa',    'joao.costa@email.com',     75, 'idoso');
INSERT INTO tb_cidadao (cd_cidadao, nm_cidadao, ds_email, nr_idade, ds_perfil) VALUES (12, 'Patricia Gomes',        'patricia.gomes@email.com', 38, 'pcd');

-- tb_ponto_descanso (16)  -  exemplos na cidade de Sao Paulo (dados ilustrativos)
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (1,  'Banco da Praca da Republica',     'Praca da Republica - Republica, Sao Paulo',          -23.543300, -46.642100, 'N', TO_DATE('2026-09-01','YYYY-MM-DD'), 1);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (2,  'Banco do Parque Trianon',         'Alameda Santos, 949 - Cerqueira Cesar, Sao Paulo',    -23.561600, -46.655400, 'N', TO_DATE('2026-09-01','YYYY-MM-DD'), 1);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (3,  'Banco da Praca da Liberdade',     'Praca da Liberdade - Liberdade, Sao Paulo',           -23.557500, -46.635300, 'N', TO_DATE('2026-09-02','YYYY-MM-DD'), 1);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (4,  'Banco do Parque da Aclimacao',    'Rua Muniz de Souza, 1119 - Aclimacao, Sao Paulo',     -23.572900, -46.628100, 'N', TO_DATE('2026-09-03','YYYY-MM-DD'), 1);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (5,  'Banco do Largo do Arouche',       'Largo do Arouche - Republica, Sao Paulo',             -23.540300, -46.644400, 'N', TO_DATE('2026-09-03','YYYY-MM-DD'), 1);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (6,  'Sombra no Parque Ibirapuera',     'Av. Pedro Alvares Cabral - Vila Mariana, Sao Paulo',  -23.587400, -46.657600, 'N', TO_DATE('2026-09-04','YYYY-MM-DD'), 2);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (7,  'Sombra na Praca Benedito Calixto','Praca Benedito Calixto - Pinheiros, Sao Paulo',        -23.566800, -46.682900, 'N', TO_DATE('2026-09-04','YYYY-MM-DD'), 2);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (8,  'Sombra no Largo da Batata',      'Largo da Batata - Pinheiros, Sao Paulo',              -23.567000, -46.693000, 'N', TO_DATE('2026-09-05','YYYY-MM-DD'), 2);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (9,  'Marquise do Conjunto Nacional',  'Av. Paulista, 2073 - Bela Vista, Sao Paulo',          -23.569000, -46.648300, 'S', TO_DATE('2026-09-05','YYYY-MM-DD'), 3);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (10, 'Marquise do Mercado Municipal',  'Rua da Cantareira, 306 - Centro, Sao Paulo',          -23.541200, -46.629500, 'S', TO_DATE('2026-09-06','YYYY-MM-DD'), 3);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (11, 'Marquise da Estacao da Luz',     'Praca da Luz - Luz, Sao Paulo',                       -23.536500, -46.633400, 'S', TO_DATE('2026-09-06','YYYY-MM-DD'), 3);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (12, 'Bebedouro do Parque da Juventude','Av. Cruzeiro do Sul, 2630 - Santana, Sao Paulo',      -23.513000, -46.625600, 'N', TO_DATE('2026-09-07','YYYY-MM-DD'), 4);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (13, 'Bebedouro do Parque Villa-Lobos', 'Av. Prof. Fonseca Rodrigues, 2001 - Alto de Pinheiros, Sao Paulo', -23.547000, -46.722800, 'N', TO_DATE('2026-09-07','YYYY-MM-DD'), 4);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (14, 'Bebedouro da Praca Roosevelt',   'Praca Franklin Roosevelt - Consolacao, Sao Paulo',    -23.549600, -46.650200, 'N', TO_DATE('2026-09-08','YYYY-MM-DD'), 4);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (15, 'Banheiro acessivel Terminal Bandeira', 'Av. Nove de Julho, s/n - Bela Vista, Sao Paulo', -23.558400, -46.639700, 'S', TO_DATE('2026-09-08','YYYY-MM-DD'), 5);
INSERT INTO tb_ponto_descanso (cd_ponto, nm_ponto, ds_endereco, vl_latitude, vl_longitude, st_coberto, dt_cadastro, cd_tipo) VALUES (16, 'Banheiro acessivel Centro',      'Rua Coronel Xavier de Toledo, 23 - Centro, Sao Paulo', -23.544200, -46.638400, 'S', TO_DATE('2026-09-09','YYYY-MM-DD'), 5);

-- tb_trajeto (8)
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (1, 'Praca da Republica',        'Praca da Liberdade',      1900, TO_DATE('2026-09-10','YYYY-MM-DD'), 1);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (2, 'Largo do Arouche',          'Mercado Municipal',       2300, TO_DATE('2026-09-10','YYYY-MM-DD'), 2);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (3, 'Parque Trianon',           'Parque Ibirapuera',       3800, TO_DATE('2026-09-11','YYYY-MM-DD'), 3);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (4, 'Praca Roosevelt',          'Conjunto Nacional',       1400, TO_DATE('2026-09-12','YYYY-MM-DD'), 4);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (5, 'Praca Benedito Calixto',   'Largo da Batata',         1200, TO_DATE('2026-09-12','YYYY-MM-DD'), 7);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (6, 'Praca da Liberdade',       'Terminal Bandeira',        900, TO_DATE('2026-09-13','YYYY-MM-DD'), 1);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (7, 'Estacao da Luz',           'Mercado Municipal',       1300, TO_DATE('2026-09-14','YYYY-MM-DD'), 6);
INSERT INTO tb_trajeto (cd_trajeto, ds_origem, ds_destino, vl_distancia_m, dt_trajeto, cd_cidadao) VALUES (8, 'Parque da Aclimacao',      'Praca da Liberdade',      1100, TO_DATE('2026-09-15','YYYY-MM-DD'), 9);

-- tb_avaliacao (10)
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (1,  5, 'Banco confortavel e com sombra de manha',   TO_DATE('2026-09-16','YYYY-MM-DD'), 1,  1);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (2,  4, 'Bom lugar para descansar no meio do trajeto', TO_DATE('2026-09-16','YYYY-MM-DD'), 1,  2);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (3,  5, 'Praca tranquila e bem cuidada',             TO_DATE('2026-09-17','YYYY-MM-DD'), 3,  1);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (4,  4, 'Sombra boa mesmo no calor da tarde',        TO_DATE('2026-09-17','YYYY-MM-DD'), 6,  3);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (5,  3, 'Sombra boa, mas faltam bancos por perto',    TO_DATE('2026-09-18','YYYY-MM-DD'), 6,  5);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (6,  5, 'Marquise larga, protege da chuva e do sol',  TO_DATE('2026-09-18','YYYY-MM-DD'), 9,  4);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (7,  2, 'Banco quebrado e muito sol',                TO_DATE('2026-09-19','YYYY-MM-DD'), 2,  7);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (8,  1, 'Bebedouro sem funcionar',                   TO_DATE('2026-09-19','YYYY-MM-DD'), 12, 6);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (9,  4, 'Boa cobertura, mas movimentado',           TO_DATE('2026-09-20','YYYY-MM-DD'), 10, 9);
INSERT INTO tb_avaliacao (cd_avaliacao, nr_nota, ds_comentario, dt_avaliacao, cd_ponto, cd_cidadao) VALUES (10, 5, 'Banheiro limpo e com rampa de acesso',     TO_DATE('2026-09-20','YYYY-MM-DD'), 15, 10);

-- tb_trajeto_ponto (9)
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (1, 1,  1);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (1, 5,  2);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (1, 3,  3);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (2, 5,  1);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (2, 11, 2);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (2, 10, 3);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (3, 2,  1);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (3, 6,  2);
INSERT INTO tb_trajeto_ponto (cd_trajeto, cd_ponto, nr_ordem) VALUES (4, 14, 1);

COMMIT;


/* ============================================================================
   3) CONSULTAS (10, numeradas) - cada uma responde uma pergunta da solucao
   ============================================================================ */

-- Consulta 1: Quais sao os bancos cadastrados, em ordem alfabetica?
-- Recursos: WHERE + ORDER BY (+ INNER JOIN)
SELECT p.cd_ponto, p.nm_ponto, p.ds_endereco
  FROM tb_ponto_descanso p
 INNER JOIN tb_tipo_ponto t ON t.cd_tipo = p.cd_tipo
 WHERE t.nm_tipo = 'banco'
 ORDER BY p.nm_ponto;

-- Consulta 2: Qual a distancia de cada trajeto em km e quanto tempo leva a pe
--             em ritmo lento (3 km/h = 50 m por minuto)?
-- Recursos: calculo no SELECT (alem de funcao de agregacao)
SELECT cd_trajeto,
       ds_origem,
       ds_destino,
       vl_distancia_m,
       vl_distancia_m / 1000          AS vl_distancia_km,
       ROUND(vl_distancia_m / 50)     AS nr_minutos_estimados
  FROM tb_trajeto
 ORDER BY vl_distancia_m DESC;

-- Consulta 3: Quantos pontos de descanso existem de cada tipo?
-- Recursos: COUNT + GROUP BY + INNER JOIN
SELECT t.nm_tipo, COUNT(p.cd_ponto) AS qt_pontos
  FROM tb_tipo_ponto t
 INNER JOIN tb_ponto_descanso p ON p.cd_tipo = t.cd_tipo
 GROUP BY t.nm_tipo
 ORDER BY qt_pontos DESC, t.nm_tipo;

-- Consulta 4: Qual a nota media de cada ponto avaliado e quantas avaliacoes tem?
-- Recursos: AVG + COUNT + GROUP BY + INNER JOIN
SELECT p.nm_ponto,
       COUNT(a.cd_avaliacao)      AS qt_avaliacoes,
       ROUND(AVG(a.nr_nota), 2)   AS vl_nota_media
  FROM tb_ponto_descanso p
 INNER JOIN tb_avaliacao a ON a.cd_ponto = p.cd_ponto
 GROUP BY p.nm_ponto
 ORDER BY vl_nota_media DESC, p.nm_ponto;

-- Consulta 5: Para cada perfil de cidadao, quantos trajetos foram planejados e
--             qual a soma, a maior e a menor distancia (em metros)?
-- Recursos: COUNT + SUM + MAX + MIN + GROUP BY + INNER JOIN
SELECT c.ds_perfil,
       COUNT(t.cd_trajeto)   AS qt_trajetos,
       SUM(t.vl_distancia_m) AS vl_total_m,
       MAX(t.vl_distancia_m) AS vl_maior_m,
       MIN(t.vl_distancia_m) AS vl_menor_m
  FROM tb_cidadao c
 INNER JOIN tb_trajeto t ON t.cd_cidadao = c.cd_cidadao
 GROUP BY c.ds_perfil
 ORDER BY vl_total_m DESC;

-- Consulta 6: Quais tipos de ponto tem nota media acima de 4?
-- Recursos: GROUP BY + HAVING + INNER JOIN
SELECT t.nm_tipo, ROUND(AVG(a.nr_nota), 2) AS vl_nota_media
  FROM tb_avaliacao a
 INNER JOIN tb_ponto_descanso p ON p.cd_ponto = a.cd_ponto
 INNER JOIN tb_tipo_ponto t     ON t.cd_tipo  = p.cd_tipo
 GROUP BY t.nm_tipo
HAVING AVG(a.nr_nota) > 4
 ORDER BY vl_nota_media DESC;

-- Consulta 7: Quais pontos tem nota media acima da nota media geral de todas as avaliacoes?
-- Recursos: SUBCONSULTA (SUBSELECT) + GROUP BY + HAVING
SELECT p.nm_ponto, ROUND(AVG(a.nr_nota), 2) AS vl_nota_media
  FROM tb_ponto_descanso p
 INNER JOIN tb_avaliacao a ON a.cd_ponto = p.cd_ponto
 GROUP BY p.nm_ponto
HAVING AVG(a.nr_nota) > (SELECT AVG(nr_nota) FROM tb_avaliacao)
 ORDER BY vl_nota_media DESC, p.nm_ponto;

-- Consulta 8: Quantas paradas tem cada trajeto e qual a distancia media entre elas?
--             (indicador de impacto do app: distancia media entre paradas)
-- Recursos: INNER JOIN (3 tabelas) + COUNT + calculo + GROUP BY
SELECT t.cd_trajeto,
       t.ds_origem,
       t.ds_destino,
       t.vl_distancia_m,
       COUNT(tp.cd_ponto)                                  AS qt_paradas,
       ROUND(t.vl_distancia_m / (COUNT(tp.cd_ponto) + 1))  AS vl_media_entre_paradas_m
  FROM tb_trajeto t
 INNER JOIN tb_trajeto_ponto tp ON tp.cd_trajeto = t.cd_trajeto
 INNER JOIN tb_ponto_descanso p ON p.cd_ponto    = tp.cd_ponto
 GROUP BY t.cd_trajeto, t.ds_origem, t.ds_destino, t.vl_distancia_m
 ORDER BY t.cd_trajeto;

-- Consulta 9: Quais pontos existem e que avaliacoes receberam (inclusive os que
--             ainda nao foram avaliados)?
-- Recursos: LEFT JOIN
SELECT p.cd_ponto, p.nm_ponto, a.cd_avaliacao, a.nr_nota, a.ds_comentario
  FROM tb_ponto_descanso p
  LEFT JOIN tb_avaliacao a ON a.cd_ponto = p.cd_ponto
 ORDER BY p.cd_ponto, a.cd_avaliacao;

-- Consulta 10: Quais cidadaos ja avaliaram algum ponto e quais ainda nao avaliaram nenhum?
-- Recursos: RIGHT JOIN
SELECT c.cd_cidadao, c.nm_cidadao, c.ds_perfil, a.cd_avaliacao, a.nr_nota
  FROM tb_avaliacao a
 RIGHT JOIN tb_cidadao c ON a.cd_cidadao = c.cd_cidadao
 ORDER BY c.nm_cidadao, a.cd_avaliacao;


/* ============================================================================
   4) UPDATE e DELETE  (ambos com WHERE)
   ============================================================================ */

-- UPDATE: o Banco do Parque da Aclimacao ganhou cobertura (st_coberto passa a 'S').
UPDATE tb_ponto_descanso
   SET st_coberto = 'S'
 WHERE cd_ponto = 4;

-- DELETE: remove as avaliacoes com nota 1 (consideradas invalidas/denuncia de defeito ja resolvido).
DELETE FROM tb_avaliacao
 WHERE nr_nota = 1;

COMMIT;
