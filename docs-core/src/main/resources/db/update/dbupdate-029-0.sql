alter table T_FILE add column FIL_SIZE_N bigint not null default -1;
update T_CONFIG set CFG_VALUE_C = '29' where CFG_ID_C = 'DB_VERSION';
