create cached table T_COMMENT ( COM_ID_C varchar(36) not null, COM_IDDOC_C varchar(36) not null, COM_IDUSER_C varchar(36) not null, COM_CONTENT_C varchar(4000) not null, COM_CREATEDATE_D datetime, COM_DELETEDATE_D datetime, primary key (COM_ID_C) );
alter table T_COMMENT add constraint FK_COM_IDDOC_C foreign key (COM_IDDOC_C) references T_DOCUMENT (DOC_ID_C) on delete restrict on update restrict;
alter table T_COMMENT add constraint FK_COM_IDUSER_C foreign key (COM_IDUSER_C) references T_USER (USE_ID_C) on delete restrict on update restrict;
update T_CONFIG set CFG_VALUE_C = '3' where CFG_ID_C = 'DB_VERSION';
