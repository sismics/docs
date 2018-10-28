create table T_ROUTE_MODEL ( RTM_ID_C varchar(36) not null, RTM_NAME_C varchar(50) not null, RTM_STEPS_C varchar(5000) not null, RTM_CREATEDATE_D datetime not null, RTM_DELETEDATE_D datetime, primary key (RTM_ID_C) );
create cached table T_ROUTE ( RTE_ID_C varchar(36) not null, RTE_IDDOCUMENT_C varchar(36) not null, RTE_NAME_C varchar(50) not null, RTE_CREATEDATE_D datetime not null, RTE_DELETEDATE_D datetime, primary key (RTE_ID_C) );
create cached table T_ROUTE_STEP ( RTP_ID_C varchar(36) not null, RTP_IDROUTE_C varchar(36) not null, RTP_NAME_C varchar(200) not null, RTP_TYPE_C varchar(50) not null, RTP_TRANSITION_C varchar(50), RTP_COMMENT_C varchar(500), RTP_IDTARGET_C varchar(36) not null, RTP_IDVALIDATORUSER_C varchar(36), RTP_ORDER_N int not null, RTP_CREATEDATE_D datetime not null, RTP_ENDDATE_D datetime, RTP_DELETEDATE_D datetime, primary key (RTP_ID_C) );
alter table T_ACL add column ACL_TYPE_C varchar(30) not null default 'USER';
alter table T_ROUTE add constraint FK_RTE_IDDOCUMENT_C foreign key (RTE_IDDOCUMENT_C) references T_DOCUMENT (DOC_ID_C) on delete restrict on update restrict;
alter table T_ROUTE_STEP add constraint FK_RTP_IDROUTE_C foreign key (RTP_IDROUTE_C) references T_ROUTE (RTE_ID_C) on delete restrict on update restrict;
alter table T_ROUTE_STEP add constraint FK_RTP_IDVALIDATORUSER_C foreign key (RTP_IDVALIDATORUSER_C) references T_USER (USE_ID_C) on delete restrict on update restrict;

insert into T_ROUTE_MODEL (RTM_ID_C, RTM_NAME_C, RTM_STEPS_C, RTM_CREATEDATE_D) values ('default-document-review', 'Document review', '[{"type":"VALIDATE","target":{"name":"administrators","type":"GROUP"},"name":"Check the document''s metadata"},{"type":"VALIDATE","target":{"name":"administrators","type":"GROUP"},"name":"Add relevant files to the document"},{"type":"APPROVE","target":{"name":"administrators","type":"GROUP"},"name":"Approve the document"}]', now());
insert into T_ACL (ACL_ID_C, ACL_PERM_C, ACL_SOURCEID_C, ACL_TARGETID_C) values ('acl-admin-default-route-read', 'READ', 'default-document-review', 'administrators');
insert into T_ACL (ACL_ID_C, ACL_PERM_C, ACL_SOURCEID_C, ACL_TARGETID_C) values ('acl-admin-default-route-write', 'WRITE', 'default-document-review', 'administrators');

update T_CONFIG set CFG_VALUE_C = '15' where CFG_ID_C = 'DB_VERSION';
