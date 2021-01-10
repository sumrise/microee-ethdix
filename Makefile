

clear:
	rm -rf dist

prepared:
	mkdir dist 2>/dev/null

dist: clear prepared
	mvn -f pom.xml clean package
	cp microee-ethdix-app/target/microee-ethdix-app-1.0-SNAPSHOT.jar dist/
	cp -rf uniswap-dixx dist/
	rm -rf dist/uniswap-dixx/node_modules
	mvn clean

run: dist
	java -jar dist/microee-ethdix-app-1.0-SNAPSHOT.jar

