
# 当前 Makefile 文件物理路径
ROOT_DIR:=$(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))

clear:
	# rm -rf `find ./dist -maxdepth 1 ! -name uniswap-dixx ! -name dist` 
	rm -rf $(ROOT_DIR)/dist

prepare:
	rm -rf $(ROOT_DIR)/dist
	mkdir -p dist 2>/dev/null

dist: clear prepare
	mvn -f $(ROOT_DIR)/pom.xml clean package
	cp $(ROOT_DIR)/microee-ethdix-app/target/microee-ethdix-app-1.0-SNAPSHOT.jar $(ROOT_DIR)/dist/
	cp $(ROOT_DIR)/microee-ethdix-web/target/microee-ethdix-web-1.0-SNAPSHOT.jar $(ROOT_DIR)/dist/
	#rsync -az --exclude 'node_modules' --exclude 'package-lock.json' uniswap-dixx dist/
	mvn clean

run: dist
	java -jar $(ROOT_DIR)/dist/microee-ethdix-app-1.0-SNAPSHOT.jar

