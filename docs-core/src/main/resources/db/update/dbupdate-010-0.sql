alter table T_FILE alter column FIL_IDUSER_C set not null;
alter table T_AUTHENTICATION_TOKEN add column AUT_IP_C varchar(45);
alter table T_AUTHENTICATION_TOKEN add column AUT_UA_C varchar(1000);
update T_CONFIG set CFG_VALUE_C='10' where CFG_ID_C='DB_VERSION';