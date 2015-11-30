alter table T_FILE alter column FIL_IDUSER_C set not null;
update T_CONFIG set CFG_VALUE_C = '5' where CFG_ID_C = 'DB_VERSION';
