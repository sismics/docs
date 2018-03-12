alter table T_DOCUMENT add column DOC_UPDATEDATE_D datetime;
update T_DOCUMENT set DOC_UPDATEDATE_D = DOC_CREATEDATE_D;
alter table T_DOCUMENT alter column DOC_UPDATEDATE_D datetime not null;
update T_CONFIG set CFG_VALUE_C = '18' where CFG_ID_C = 'DB_VERSION';
