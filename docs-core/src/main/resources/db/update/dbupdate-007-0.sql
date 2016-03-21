create cached table T_RELATION ( REL_ID_C varchar(36) not null, REL_IDDOCFROM_C varchar(36) not null, REL_IDDOCTO_C varchar(36) not null, REL_DELETEDATE_D datetime, primary key (REL_ID_C) );

update T_CONFIG set CFG_VALUE_C = '7' where CFG_ID_C = 'DB_VERSION';
