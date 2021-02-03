

clear:
	# rm -rf `find ./dist -maxdepth 1 ! -name uniswap-dixx ! -name dist` 
	rm -rf dist

prepare:
	mkdir -p dist 2>/dev/null

dist: clear prepare
	mvn -f pom.xml clean package
	cp microee-ethdix-app/target/microee-ethdix-app-1.0-SNAPSHOT.jar dist/
	cp microee-ethdix-web/target/microee-ethdix-web-1.0-SNAPSHOT.jar dist/
	#rsync -az --exclude 'node_modules' --exclude 'package-lock.json' uniswap-dixx dist/
	mvn clean

run: dist
	java -jar dist/microee-ethdix-app-1.0-SNAPSHOT.jar

