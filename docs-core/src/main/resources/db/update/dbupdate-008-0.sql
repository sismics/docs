create memory table T_GROUP ( GRP_ID_C varchar(36) not null, GRP_IDPARENT_C varchar(36), GRP_NAME_C varchar(50) not null, GRP_IDROLE_C varchar(36), GRP_DELETEDATE_D datetime, primary key (GRP_ID_C) );
create memory table T_USER_GROUP ( UGP_ID_C varchar(36) not null, UGP_IDUSER_C varchar(36) not null, UGP_IDGROUP_C varchar(36) not null, UGP_DELETEDATE_D datetime, primary key (UGP_ID_C) );

insert into T_GROUP(GRP_ID_C, GRP_NAME_C, GRP_IDROLE_C) values('administrators', 'administrators', 'admin');
insert into T_USER_GROUP(UGP_ID_C, UGP_IDUSER_C, UGP_IDGROUP_C) values('admin-administrators', 'admin', 'administrators');

update T_CONFIG set CFG_VALUE_C = '8' where CFG_ID_C = 'DB_VERSION';
