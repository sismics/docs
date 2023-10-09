create index IDX_FIL_IDDOC_C ON T_FILE (FIL_IDDOC_C ASC);
update T_CONFIG set CFG_VALUE_C = '30' where CFG_ID_C = 'DB_VERSION';
