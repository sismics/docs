alter table T_DOCUMENT alter column DOC_LANGUAGE_C set varchar(7) default 'eng' not null;
update T_CONFIG set CFG_VALUE_C = '12' where CFG_ID_C = 'DB_VERSION';
