create table T_ROUTE_MODEL ( RTM_ID_C varchar(36) not null, RTM_NAME_C varchar(50) not null, RTM_STEPS_C varchar(5000) not null, RTM_CREATEDATE_D datetime not null, RTM_DELETEDATE_D datetime, primary key (RTM_ID_C) );
create cached table T_ROUTE ( RTE_ID_C varchar(36) not null, RTE_IDDOCUMENT_C varchar(36) not null, RTE_CREATEDATE_D datetime not null, RTE_DELETEDATE_D datetime, primary key (RTE_ID_C) );
create cached table T_ROUTE_STEP ( RTP_ID_C varchar(36) not null, RTP_IDROUTE_C varchar(36) not null, RTP_NAME_C varchar(200) not null, RTP_TYPE_C varchar(50) not null, RTP_TRANSITION_C varchar(50), RTP_COMMENT_C varchar(500), RTP_IDTARGET_C varchar(36) not null, RTP_ORDER_N int not null, RTE_CREATEDATE_D datetime not null, RTP_ENDDATE_D datetime, RTP_DELETEDATE_D datetime, primary key (RTP_ID_C) );;
alter table T_ROUTE add constraint FK_RTE_IDDOCUMENT_C foreign key (RTE_IDDOCUMENT_C) references T_DOCUMENT (DOC_ID_C) on delete restrict on update restrict;
alter table T_ROUTE_STEP add constraint FK_RTP_IDROUTE_C foreign key (RTP_IDROUTE_C) references T_ROUTE (RTE_ID_C) on delete restrict on update restrict;
update T_CONFIG set CFG_VALUE_C = '15' where CFG_ID_C = 'DB_VERSION';
