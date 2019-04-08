#!/bin/bash
if [ -a ./.gh-pages ] 
then 
	rm -rf ./.gh-pages
else 
	mkdir ./.gh-pages
fi

git clone -b gh-pages-web --depth 1 https://github.com/Ericsson/papyrus-patches.git ./.gh-pages
if [ $? -ne 0 ] 
then
    echo "Fail to clone web branch."
    exit $?
fi

mkdir -p ./.gh-pages/download/updates/nightly/
cp -rf ./releng/com.ericsson.papyrus.patches.update-site/target/site ./.gh-pages/download/updates/nightly/

exit $?
 