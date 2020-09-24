alter table T_FILE add column FIL_VERSION_N int not null default 0;
alter table T_FILE add column FIL_LATESTVERSION_B bit not null default 1;
alter table T_FILE add column FIL_IDVERSION_C varchar(36);

update T_CONFIG set CFG_VALUE_C = '22' where CFG_ID_C = 'DB_VERSION';
