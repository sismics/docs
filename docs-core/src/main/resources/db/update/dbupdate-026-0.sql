!PGSQL!UPDATE t_file SET fil_content_c = convert_from(loread(lo_open(fil_content_c::int, CAST( x'20000' AS integer)), 999999999), 'UNICODE')::TEXT WHERE fil_content_c IS NOT NULL;
update T_CONFIG set CFG_VALUE_C = '26' where CFG_ID_C = 'DB_VERSION';
