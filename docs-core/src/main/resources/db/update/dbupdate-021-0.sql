alter table T_DOCUMENT add column DOC_IDFILE_C varchar(36);
alter table T_DOCUMENT add constraint FK_DOC_IDFILE_C foreign key (DOC_IDFILE_C) references T_FILE (FIL_ID_C) on delete restrict on update restrict;

update T_CONFIG set CFG_VALUE_C = '21' where CFG_ID_C = 'DB_VERSION';
