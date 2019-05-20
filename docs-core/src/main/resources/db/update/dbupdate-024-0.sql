create cached table T_METADATA ( MET_ID_C varchar(36) not null, MET_NAME_C varchar(50) not null, MET_TYPE_C varchar(20) not null, MET_DELETEDATE_D datetime, primary key (MET_ID_C) );
create cached table T_DOCUMENT_METADATA ( DME_ID_C varchar(36) not null, DME_IDDOCUMENT_C varchar(36) not null, DME_IDMETADATA_C varchar(36) not null, DME_VALUE_C varchar(4000) null, primary key (DME_ID_C) );
alter table T_DOCUMENT_METADATA add constraint FK_DME_IDDOCUMENT_C foreign key (DME_IDDOCUMENT_C) references T_DOCUMENT (DOC_ID_C) on delete restrict on update restrict;
alter table T_DOCUMENT_METADATA add constraint FK_DME_IDMETADATA_C foreign key (DME_IDMETADATA_C) references T_METADATA (MET_ID_C) on delete restrict on update restrict;
update T_CONFIG set CFG_VALUE_C = '24' where CFG_ID_C = 'DB_VERSION';
