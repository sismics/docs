!H2!alter table T_DOCUMENT alter column DOC_LANGUAGE_C varchar(7) default 'eng' not null;
!PGSQL!alter table T_DOCUMENT alter column DOC_LANGUAGE_C type varchar(7);
!PGSQL!alter table T_DOCUMENT alter column DOC_LANGUAGE_C set default 'eng';
!PGSQL!alter table T_DOCUMENT alter column DOC_LANGUAGE_C set not null;
update T_CONFIG set CFG_VALUE_C = '12' where CFG_ID_C = 'DB_VERSION';
