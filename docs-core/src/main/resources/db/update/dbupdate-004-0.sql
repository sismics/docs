create cached table T_SHARE ( SHA_ID_C varchar(36) not null, SHA_NAME_C varchar(36), SHA_IDDOCUMENT_C varchar(36) not null, SHA_CREATEDATE_D datetime, SHA_DELETEDATE_D datetime, primary key (SHA_ID_C) );
update T_CONFIG set CFG_VALUE_C='4' where CFG_ID_C='DB_VERSION';
