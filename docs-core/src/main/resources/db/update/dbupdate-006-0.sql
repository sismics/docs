alter table T_USER add column USE_PRIVATEKEY_C varchar(100) default '' not null;
update T_USER set USE_PRIVATEKEY_C = 'AdminPk' where USE_ID_C = 'admin';
update T_CONFIG set CFG_VALUE_C='6' where CFG_ID_C='DB_VERSION';
