### ethdix 微服务 -- ethdix parent
$ mvn archetype:generate -DgroupId=com.microee.ethdix -DartifactId=microee-ethdix -DarchetypeArtifactId=pom-root -DinteractiveMode=false -DarchetypeGroupId=org.codehaus.mojo.archetypes -DarchetypeVersion=RELEASE -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-loop 区块扫描
mvn archetype:generate -DgroupId=com.microee.ethdix.loop -DartifactId=microee-ethdix-loop -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-oem 领域模型
mvn archetype:generate -DgroupId=com.microee.ethdix.oem -DartifactId=microee-ethdix-oem -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-j3 合约解析
mvn archetype:generate -DgroupId=com.microee.ethdix.j3 -DartifactId=microee-ethdix-j3 -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-app 对外接口
mvn archetype:generate -DgroupId=com.microee.ethdix.app -DartifactId=microee-ethdix-app -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-rmi 远程调用
mvn archetype:generate -DgroupId=com.microee.ethdix.rmi -DartifactId=microee-ethdix-rmi -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local

### ethdix 微服务 -- ethdix-interfaces 远程调用
mvn archetype:generate -DgroupId=com.microee.ethdix.interfaces -DartifactId=microee-ethdix-interfaces -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false -DarchetypeCatalog=local


### 合约查询浏览器
# blockchair：https://blockchair.com/zh/ethereum/erc-20

### How to scan the latest pairs added to Uniswap ?
# https://docs.google.com/spreadsheets/d/1jKEhOi9gIcM9bKdn7rgJEK0RKpzbE1k6bPy_kJW75Aw/edit#gid=1707981752
# https://medium.com/coinmonks/how-to-scan-the-latest-pairs-added-to-uniswap-31c4400cc2a6

### Uniswap v2 (as a Developer)
# https://vomtom.at/how-to-use-uniswap-v2-as-a-developer/

### uniswap-pool-api
# https://bitquery.io/blog/uniswap-pool-api

### Trading
# https://uniswap.org/docs/v2/javascript-SDK/trading/

### Smart Contract Quick start
# https://uniswap.org/docs/v2/smart-contract-integration/quick-start/

### Effective way to get all Uniswap exchange addresses?
# https://www.reddit.com/r/UniSwap/comments/atddo2/effective_way_to_get_all_uniswap_exchange/

### How to scan the latest pairs added to Uniswap ?
# 通过订阅 UniSwap 工厂合约的 Events 事件
# https://etherscan.io/address/0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f#events
# V1 合约地址 0xc0a47dFe034B400B47bDaD5FecDa2621de6c4d95
# v2 合约地址 0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f

### Biance Crypto Exchange - Volume, Market Prices & Listings, Trading Pairs
# https://nomics.com/exchanges/binance#about

### 什么是abi
# 应用程序二进制接口，以太坊的调用合约时的接口说明
# ABI是两个程序模块之间的接口，主要是用于将数据编码或解码为源代码所表示的代码。
# 以太坊中主要用于solidity合约的函数调用，以及反向编码读取数据的中的方法
# https://blog.csdn.net/qq_35434814/article/details/104682616

### Application Binary Interface
# https://docs.web3j.io/smart_contracts/application_binary_interface/

### ABI codec
# https://github.com/dolomite-exchange/abi-encoder-v2-java

