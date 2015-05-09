create cached table T_ACL ( ACL_ID_C varchar(36) not null, ACL_PERM_C varchar(30) not null, ACL_SOURCEID_C varchar(36) not null, ACL_TARGETID_C varchar(36) not null, ACL_DELETEDATE_D datetime, primary key (ACL_ID_C) );
drop table T_SHARE;
create cached table T_SHARE ( SHA_ID_C varchar(36) not null, SHA_NAME_C varchar(36), SHA_CREATEDATE_D datetime, SHA_DELETEDATE_D datetime, primary key (SHA_ID_C) );
update T_CONFIG set CFG_VALUE_C='8' where CFG_ID_C='DB_VERSION';
