alter table T_DOCUMENT add column DOC_SUBJECT_C varchar(500);
alter table T_DOCUMENT add column DOC_IDENTIFIER_C varchar(500);
update T_CONFIG set CFG_VALUE_C = '6' where CFG_ID_C = 'DB_VERSION';
