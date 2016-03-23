alter table T_USER add column USE_TOTPKEY_C varchar(100);
update T_CONFIG set CFG_VALUE_C = '9' where CFG_ID_C = 'DB_VERSION';
