alter table T_FILE alter column FIL_IDUSER_C set not null;
alter table T_AUTHENTICATION_TOKEN add column AUT_IP_C varchar(45);
alter table T_AUTHENTICATION_TOKEN add column AUT_UA_C varchar(1000);
create cached table T_AUDIT_LOG ( LOG_ID_C varchar(36) not null, LOG_IDENTITY_C varchar(36) not null, LOG_CLASSENTITY_C varchar(50) not null, LOG_TYPE_C varchar(50) not null, LOG_MESSAGE_C varchar(1000), LOG_CREATEDATE_D datetime, primary key (LOG_ID_C) );
create index IDX_LOG_COMPOSITE on T_AUDIT_LOG (LOG_IDENTITY_C, LOG_CLASSENTITY_C);
alter table T_DOCUMENT_TAG add column DOT_DELETEDATE_D datetime;
update T_CONFIG set CFG_VALUE_C='10' where CFG_ID_C='DB_VERSION';