alter table T_FILE alter column FIL_IDDOC_C set null;
alter table T_FILE add column FIL_IDUSER_C varchar(36);
update T_CONFIG set CFG_VALUE_C='7' where CFG_ID_C='DB_VERSION';
