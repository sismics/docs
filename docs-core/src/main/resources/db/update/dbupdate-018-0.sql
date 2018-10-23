alter table T_DOCUMENT add column DOC_UPDATEDATE_D datetime;
update T_DOCUMENT set DOC_UPDATEDATE_D = DOC_CREATEDATE_D;
!H2!alter table T_DOCUMENT alter column DOC_UPDATEDATE_D datetime not null;
!PGSQL!alter table T_DOCUMENT alter column DOC_UPDATEDATE_D type timestamp;
!PGSQL!alter table T_DOCUMENT alter column DOC_UPDATEDATE_D set not null;
alter table T_ROUTE_STEP add column RTP_TRANSITIONS_C varchar(2000);
update T_CONFIG set CFG_VALUE_C = '18' where CFG_ID_C = 'DB_VERSION';
