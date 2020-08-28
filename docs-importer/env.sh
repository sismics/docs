#!/bin/ash
file=/root/.config/preferences/com.sismics.docs.importer.pref
sed -i "s/env1/$TEEDY_TAG/g" $file
sed -i "s/env2/$TEEDY_ADDTAGS/g" $file
sed -i "s/env3/$TEEDY_LANG/g" $file
sed -i "s,env4,$TEEDY_URL,g" $file
sed -i "s/env5/$TEEDY_USERNAME/g" $file
sed -i "s/env6/$TEEDY_PASSWORD/g" $file
sed -i "s,env7,$TEEDY_COPYFOLDER,g" $file
sed -i "s,env8,$TEEDY_FILEFILTER,g" $file
echo "Environment variables replaced"