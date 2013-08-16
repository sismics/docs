alter table T_FILE add column FIL_CONTENT_C LONGVARCHAR;
alter table T_DOCUMENT add column DOC_LANGUAGE_C varchar(3) default 'fra' not null;
update T_CONFIG set CFG_VALUE_C='5' where CFG_ID_C='DB_VERSION';
