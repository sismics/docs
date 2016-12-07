alter table T_FILE add column FIL_NAME_C varchar(200);
update T_CONFIG set CFG_VALUE_C = '11' where CFG_ID_C = 'DB_VERSION';
