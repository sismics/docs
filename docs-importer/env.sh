#!/bin/ash
file=/root/.config/preferences/com.sismics.docs.importer.pref
sed -i "s/env1/$tag/g" $file
sed -i "s/env2/$addTags/g" $file
sed -i "s/env3/$lang/g" $file
sed -i "s,env4,$baseUrl,g" $file
sed -i "s/env5/$username/g" $file
sed -i "s/env6/$password/g" $file
echo "Environment variables replaced"