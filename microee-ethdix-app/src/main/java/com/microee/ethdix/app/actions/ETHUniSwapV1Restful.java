package com.microee.ethdix.app.actions;

import com.microee.ethdix.app.components.Web3JFactory;
import com.microee.ethdix.j3.Constrants;
import com.microee.ethdix.j3.contract.RemoteCallFunction;
import com.microee.ethdix.j3.uniswap.UniswapV1FactoryContract;
import com.microee.ethdix.oem.eth.enums.ChainId;
import com.microee.plugin.response.R;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// V1 开发者文档
// https://uniswap.org/docs/v1/
//@formatter:off
// 实现代币交易的第三种方法是:
// 在多个ERC20代币之间进行交易, 直至获得我们想要买入的代币为止
// 对于没有直接交易对的代币，可以通过一系列中间代币交易来获得输出代币
// 如果你想买入的代币和你想卖出的代币之间没有流动性，就可以采取这种方法
// 虽然这种方法是可行的，但是目前没有智能合约能自动实现这么多笔交易，只能手动操作，并对 Uniswap 智能合约进行多个调用
// 这个方法使用频率可能不高，因为在通过一至2个中间代币转移价值后，额外的gas费会让迂回交易变得很不划算。
// 或许通过中心化交易所交易成本反而更低。
// 尽管如此一旦ETH2.0主网上线，采用更加高效的PoS算法，再加上可扩展性功能，让迂回交易变得更加可行，这种方法将得到更广泛的应用。
// #### 价格信息传输机制
// https://uniswap.org/docs/v2/core-concepts/oracles/
// 虽然 UniSwap 提供代币价格，但是它不会在链上存储任何历史价格。需要开发者自选基于一段时间的累计价格来计算某个代币在这段时间的平均价格。
// 通过某个代币在几个区块内（也可以表示成两个时间戳之间产生的区块）的价格波动性来计算该代币的平均价格。
// 这些平均价格称为时间加权平均价格(Time Weighted Average Price, TWAP)
// 所谓的TWAP就是在链上选定一段区块作为时间区间，将某个代币在这段区块内的累计价格（该代币在每个区块的价格）除以时间戳区间（结束区块的时间戳减去起始区块的时间戳），得出该代币在这段区块的平均价格。
// TWAP 是可靠的，反应了一段时间内的特定代币对的代币价格，采取这种方法可以避免闪电崩盘和剧烈的价格波动，这些在加密货币市场上很常见。当市场出现价格波动时，TWAP可以更准确的反应代币的情况。
// 如果需要开发者可以通过 UniSwap 的 JavaScript SDK 免费获取价格信息
// #### 闪电交易（Flash Swap）
// 闪电交易指的是通过一个交易来完成从 UniSwap 的流动性池中借出代币、使用这些代币进行某项操作后偿还这些代币的一个多阶段流程。
// 如果这个多阶段流程中的任意阶段失败，所有状态更改都会撤销，相关代币重新回到对应的 UniSwap 流动性池中。
// 通俗的说就是交易者不持有代币而执行闪电交易，这是因为从流动性池中借出的代币一定会归还到池内，要么交易失败，要么交易者归还借出的代币。
// 闪电交易的一大用例就是套利交易，而且交易者一定能在获利的同时将之前借得的代币价值归还至 UniSwap 流动性池内，交易者每次都能通过套利交易轻松获得收益。
// 另一个用例是使用 UniSwap 流动性结算 Maker 金库，你可以偿还债务，并取出 Maker 金库中作为担保品的 ETH （或其他代币）来偿还 UniSwap 流动性池。相比直接使用自己持有的代币来还款，这种方式消耗的 gas 更少。
// 在交易机器人这一用例中，闪电交易还可以用来自动执行套利交易。交易机器人不需要资金来执行交易，只需要识别套利机会并执行闪电交易。
// #### API
// API 不能自动执行交易，只能帮你准备一笔交易，把你愿意接受的市场价格中位数，和交换所得的最低数量（也就是所谓的“滑点”考虑进去）
// 交易准备好之后用户必须手动使用钱包软件发起和签名交易
// #### 关于 UniSwap 的代币列表
// 该代币列表是人工维护的，UniSwap 团队会用视频会议讨论要加入的的代币。
// 如果你是某代币的发行者，你想要你的代币出现在 UniSwap.exchange 默认的代币列表上，你需要在 Default Token List repository 上提交一个 GitHub Issue
// #### 总结 UniSwap V2
// 更高效的代币互换
// 按时间来加权的价格信息标识机制
// 闪电兑换功能
// UniSwap 保持了免信任、去中心化的本色，它存在于以太坊区块链上。
// UniSwap 没有没有实时订单薄功能，因此交易员想使用交易策略还是需要到交易所去交易。
// UniSwap 需要用户套利来保持交易所内代币价格与市场价格的一致。中心话交易所仍旧在平衡 UniSwap 的汇率上发挥着不可替代的作用。DeFi 替代 CeFi 还是长路慢慢。
// 等等....
// #### 在V1中, 将代币A兑换成代币B: 
// 必须先用代币A买入ETH, 再用ETH买入代币B. 因为交易者需要支付两笔交易费和gas费
// 即以 ETH 作为中间代币实现间接互换, 与之对应的智能合约的方法是: 
// swapExactETHForTokens
// swapETHForExactTokens
// swapTokensForExactETH
// swapExactTokenForETH
// https://uniswap.org/
// https://blog.csdn.net/shebao3333/article/details/107012482
// #### UniSwap 架构概述
// 在 UniSwap 的架构中只要包含两种类型的组件：工厂合约和代币交换合约
// 工厂合约/Factory Contract 用于管理代币交换合约，它负责创建新的代币交换合约并维护一个从代币地址到代币交换合约实例的映射表每一种ERC20代币都有自己的 代币交换合约/Exchange Contract，该合约负责完成两种代币的交换。
// ------------------------------------------------------
// 从开发者角度来看，将一个ERC20代币接入 Uniswap 协议的流程如下: 
// ------------------------------------------------------
// 1. 将ERC20代币协议部署到以太坊网络, 例如: token: KBB
// 2. 利用 Uniswap 的工厂合约为前面部署的 KBB 代币创建一个交换合约
// 3. 使用交换合约实现自己的ERC20代币与其他代币的交换
// ------------------------------------------------------
// 在 UniSwapV1 中每个代币交换合约只能用来将对应的ERC20代币与以太币交换。为了完成两种不同的ERC20代币的的交换，Uniswap使用以太币作为中间的桥梁，例如，先把Token A换成以太币，再将以太币换成Token B。
// #### 一旦你的ERC20代币创建了 UniSwap 交换协议[调用 Uniswap 部署的工厂合约的 createExchange() 方法], 你将得到该代币的交换合约地址, 进一步你需要为你的 Uniswap 代币交换协议添加初始流动性:
// 我们的 KBB 代币合约地址: 0xCC4d8eCFa6a5c1a84853EC5c0c08Cc54Cb177a6A
// UniSwap 上的 KBB 交换合约地址: 0x416F1Ac032D1eEE743b18296aB958743B1E61E81
// ### 添加流动性
// Uniswap 代币交换合约需要预存一些代币以便提供初始的流动性。为此，我们首先需要授权代币交换合约可以操作我们钱包中的 ERC20 代币，然后再调用代币交换合约的 addLiquidity() 方法同时存入 ERC20 代币和以太币：
// token.approve(exchange_address, TOKEN_RESERVE)
// exchange.addLiquidity(0, TOKEN_RESERVE, DEADLINE, transact={‘value’: ETH_RESERVE})
// #### 现在我们可以用 Uniswap 来实现以太币/KBB 代币或者两种 ERC20 代币之间的交换了。[即兑换]
// 1. 用 eth 换 KBB: 其实就是发送一笔交易, to 地址就是`KBB交换合约`地址
// 2. 交换两种不同的ERC20代币, 例如: 将 usdt 换成 KBB
//@formatter:on
// 1.create.exchange.js 
//@formatter:off
/*
    let Web3 = require("web3");
    const Tx = require('ethereumjs-tx')
    var abi = '[{"name":"NewExchange","inputs":[{"type":"address","name":"token","indexed":true},{"type":"address","name":"exchange","indexed":true}],"anonymous":false,"type":"event"},{"name":"initializeFactory","outputs":[],"inputs":[{"type":"address","name":"template"}],"constant":false,"payable":false,"type":"function","gas":35725},{"name":"createExchange","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"token"}],"constant":false,"payable":false,"type":"function","gas":187911},{"name":"getExchange","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"token"}],"constant":true,"payable":false,"type":"function","gas":715},{"name":"getToken","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"exchange"}],"constant":true,"payable":false,"type":"function","gas":745},{"name":"getTokenWithId","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"uint256","name":"token_id"}],"constant":true,"payable":false,"type":"function","gas":736},{"name":"exchangeTemplate","outputs":[{"type":"address","name":"out"}],"inputs":[],"constant":true,"payable":false,"type":"function","gas":633},{"name":"tokenCount","outputs":[{"type":"uint256","name":"out"}],"inputs":[],"constant":true,"payable":false,"type":"function","gas":663}]'
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    // the account address that will send the test transaction
    const addressFrom = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c'
    const privKey = process.env.privateKey
    // the Uniswap factory contract address
    const addressTo = '0xf5D915570BC477f9B8D6C0E980aA81757A3AaC36'
    const contract = new web3.eth.Contract(JSON.parse(abi), addressTo);
    const tx = contract.methods.createExchange('0xCC4d8eCFa6a5c1a84853EC5c0c08Cc54Cb177a6A');
    const encodedABI = tx.encodeABI();
    
    function sendSigned(txData, cb) {
      const privateKey = new Buffer(privKey, 'hex')
      const transaction = new Tx(txData)
      transaction.sign(privateKey)
      const serializedTx = transaction.serialize().toString('hex')
      web3.eth.sendSignedTransaction('0x' + serializedTx, cb)
    }
    // get the number of transactions sent so far so we can create a fresh nonce
    web3.eth.getTransactionCount(addressFrom).then(txCount => {
      // construct the transaction data
      const txData = {
        nonce: web3.utils.toHex(txCount),
        gasLimit: web3.utils.toHex(6000000),
        gasPrice: web3.utils.toHex(10000000000),
        to: addressTo,
        from: addressFrom,
        data: encodedABI
      }
      // fire away!
      sendSigned(txData, function(err, result) {
        if (err) return console.log('error', err)
        console.log('sent', result)
      })
    })
 */
// @formatter:on
//2.get.exchange.address.js
//@formatter:off
/*
    let fs = require("fs");
    let Web3 = require("web3");
    var abi = '[{"name":"NewExchange","inputs":[{"type":"address","name":"token","indexed":true},{"type":"address","name":"exchange","indexed":true}],"anonymous":false,"type":"event"},{"name":"initializeFactory","outputs":[],"inputs":[{"type":"address","name":"template"}],"constant":false,"payable":false,"type":"function","gas":35725},{"name":"createExchange","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"token"}],"constant":false,"payable":false,"type":"function","gas":187911},{"name":"getExchange","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"token"}],"constant":true,"payable":false,"type":"function","gas":715},{"name":"getToken","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"address","name":"exchange"}],"constant":true,"payable":false,"type":"function","gas":745},{"name":"getTokenWithId","outputs":[{"type":"address","name":"out"}],"inputs":[{"type":"uint256","name":"token_id"}],"constant":true,"payable":false,"type":"function","gas":736},{"name":"exchangeTemplate","outputs":[{"type":"address","name":"out"}],"inputs":[],"constant":true,"payable":false,"type":"function","gas":633},{"name":"tokenCount","outputs":[{"type":"uint256","name":"out"}],"inputs":[],"constant":true,"payable":false,"type":"function","gas":663}]'
    var token = '0xCC4d8eCFa6a5c1a84853EC5c0c08Cc54Cb177a6A'
    var address = '0xf5D915570BC477f9B8D6C0E980aA81757A3AaC36';
    const account = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c';
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    const uniswap = new web3.eth.Contract(JSON.parse(abi), address);
    async function call(transaction) {
        return await transaction.call({from: account});
    }
    async function getTokenExchange() {
        let exchange = await call(uniswap.methods.getExchange(token));
        console.log("the exchange address for ERC20 token is:" + exchange)
    }
    getTokenExchange()
 */
//@formatter:on
// 3.approve.exchange.js
//@formatter:off
/*
    let Web3 = require("web3");
    const Tx = require('ethereumjs-tx')
    
    var abi = '[{"constant":true,"inputs":[],"name":"name","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x06fdde03"},{"constant":false,"inputs":[{"name":"spender","type":"address"},{"name":"value","type":"uint256"}],"name":"approve","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x095ea7b3"},{"constant":true,"inputs":[],"name":"totalSupply","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x18160ddd"},{"constant":false,"inputs":[{"name":"from","type":"address"},{"name":"to","type":"address"},{"name":"value","type":"uint256"}],"name":"transferFrom","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x23b872dd"},{"constant":true,"inputs":[],"name":"decimals","outputs":[{"name":"","type":"uint8"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x313ce567"},{"constant":true,"inputs":[],"name":"cap","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x355274ea"},{"constant":false,"inputs":[{"name":"spender","type":"address"},{"name":"addedValue","type":"uint256"}],"name":"increaseAllowance","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x39509351"},{"constant":false,"inputs":[{"name":"to","type":"address"},{"name":"value","type":"uint256"}],"name":"mint","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x40c10f19"},{"constant":true,"inputs":[{"name":"owner","type":"address"}],"name":"balanceOf","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x70a08231"},{"constant":false,"inputs":[],"name":"renounceOwnership","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x715018a6"},{"constant":true,"inputs":[],"name":"owner","outputs":[{"name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x8da5cb5b"},{"constant":true,"inputs":[],"name":"isOwner","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x8f32d59b"},{"constant":true,"inputs":[],"name":"symbol","outputs":[{"name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function","signature":"0x95d89b41"},{"constant":false,"inputs":[{"name":"account","type":"address"}],"name":"addMinter","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x983b2d56"},{"constant":false,"inputs":[],"name":"renounceMinter","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0x98650275"},{"constant":false,"inputs":[{"name":"spender","type":"address"},{"name":"subtractedValue","type":"uint256"}],"name":"decreaseAllowance","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0xa457c2d7"},{"constant":false,"inputs":[{"name":"to","type":"address"},{"name":"value","type":"uint256"}],"name":"transfer","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0xa9059cbb"},{"constant":true,"inputs":[{"name":"account","type":"address"}],"name":"isMinter","outputs":[{"name":"","type":"bool"}],"payable":false,"stateMutability":"view","type":"function","signature":"0xaa271e1a"},{"constant":true,"inputs":[{"name":"owner","type":"address"},{"name":"spender","type":"address"}],"name":"allowance","outputs":[{"name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function","signature":"0xdd62ed3e"},{"constant":false,"inputs":[{"name":"newOwner","type":"address"}],"name":"transferOwnership","outputs":[],"payable":false,"stateMutability":"nonpayable","type":"function","signature":"0xf2fde38b"},{"inputs":[],"payable":false,"stateMutability":"nonpayable","type":"constructor","signature":"constructor"},{"anonymous":false,"inputs":[{"indexed":true,"name":"account","type":"address"}],"name":"MinterAdded","type":"event","signature":"0x6ae172837ea30b801fbfcdd4108aa1d5bf8ff775444fd70256b44e6bf3dfc3f6"},{"anonymous":false,"inputs":[{"indexed":true,"name":"account","type":"address"}],"name":"MinterRemoved","type":"event","signature":"0xe94479a9f7e1952cc78f2d6baab678adc1b772d936c6583def489e524cb66692"},{"anonymous":false,"inputs":[{"indexed":true,"name":"from","type":"address"},{"indexed":true,"name":"to","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Transfer","type":"event","signature":"0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"},{"anonymous":false,"inputs":[{"indexed":true,"name":"owner","type":"address"},{"indexed":true,"name":"spender","type":"address"},{"indexed":false,"name":"value","type":"uint256"}],"name":"Approval","type":"event","signature":"0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"},{"anonymous":false,"inputs":[{"indexed":true,"name":"previousOwner","type":"address"},{"indexed":true,"name":"newOwner","type":"address"}],"name":"OwnershipTransferred","type":"event","signature":"0x8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e0"}]'
    
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    // the address that will send the test transaction
    const addressFrom = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c'
    const exchange_addr = '0x416F1Ac032D1eEE743b18296aB958743B1E61E81'
    const privKey = process.env.privateKey
    // the exchange contract address
    const addressTo = '0xCC4d8eCFa6a5c1a84853EC5c0c08Cc54Cb177a6A'
    const contract = new web3.eth.Contract(
      JSON.parse(abi),
      addressTo
    );
    
    const TOKEN_ADDED = web3.utils.toHex(100*10**18) 
    const tx = contract.methods.approve(exchange_addr, TOKEN_ADDED);
    const encodedABI = tx.encodeABI();
    
    // Signs the given transaction data and sends it.
    function sendSigned(txData, cb) {
      const privateKey = new Buffer(privKey, 'hex')
      const transaction = new Tx(txData)
      transaction.sign(privateKey)
      const serializedTx = transaction.serialize().toString('hex')
      web3.eth.sendSignedTransaction('0x' + serializedTx, cb)
    }
    
    // get the number of transactions sent so far so we can create a fresh nonce
    web3.eth.getTransactionCount(addressFrom).then(txCount => {
      // construct the transaction data
      const txData = {
        nonce: web3.utils.toHex(txCount),
        gasLimit: web3.utils.toHex(6000000),
        gasPrice: web3.utils.toHex(10000000000), // 10 Gwei
        to: addressTo,
        from: addressFrom,
        data: encodedABI,
      }
      // fire away!
      sendSigned(txData, function(err, result) {
        if (err) return console.log('error', err)
        console.log('sent', result)
      })
    })
 */
//@formatter:on
// 4.add.liquidity.js
//@formatter:off
/*
    let Web3 = require("web3");
    const Tx = require('ethereumjs-tx')
    
    var abi = '[{"name": "TokenPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "eth_sold", "indexed": true}, {"type": "uint256", "name": "tokens_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "EthPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "tokens_sold", "indexed": true}, {"type": "uint256", "name": "eth_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "AddLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "RemoveLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "Transfer", "inputs": [{"type": "address", "name": "_from", "indexed": true}, {"type": "address", "name": "_to", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "Approval", "inputs": [{"type": "address", "name": "_owner", "indexed": true}, {"type": "address", "name": "_spender", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "setup", "outputs": [], "inputs": [{"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 175875}, {"name": "addLiquidity", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_liquidity"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 82605}, {"name": "removeLiquidity", "outputs": [{"type": "uint256", "name": "out"}, {"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "amount"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 116814}, {"name": "__default__", "outputs": [], "inputs": [], "constant": false, "payable": true, "type": "function"}, {"name": "ethToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 12757}, {"name": "ethToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 12965}, {"name": "ethToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 50463}, {"name": "ethToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 50671}, {"name": "tokenToEthSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 47503}, {"name": "tokenToEthTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 47712}, {"name": "tokenToEthSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 50175}, {"name": "tokenToEthTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 50384}, {"name": "tokenToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51007}, {"name": "tokenToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51098}, {"name": "tokenToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 54928}, {"name": "tokenToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 55019}, {"name": "tokenToExchangeSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49342}, {"name": "tokenToExchangeTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49532}, {"name": "tokenToExchangeSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53233}, {"name": "tokenToExchangeTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53423}, {"name": "getEthToTokenInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5542}, {"name": "getEthToTokenOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6872}, {"name": "getTokenToEthInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5637}, {"name": "getTokenToEthOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6897}, {"name": "tokenAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1413}, {"name": "factoryAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1443}, {"name": "balanceOf", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}], "constant": true, "payable": false, "type": "function", "gas": 1645}, {"name": "transfer", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 75034}, {"name": "transferFrom", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_from"}, {"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 110907}, {"name": "approve", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_spender"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 38769}, {"name": "allowance", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}, {"type": "address", "name": "_spender"}], "constant": true, "payable": false, "type": "function", "gas": 1925}, {"name": "name", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1623}, {"name": "symbol", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1653}, {"name": "decimals", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1683}, {"name": "totalSupply", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1713}]'
    
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    // the address that will send the test transaction
    const addressFrom = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c'
    const privKey = process.env.privateKey
    // the exchange contract address
    const addressTo = '0x416F1Ac032D1eEE743b18296aB958743B1E61E81'
    const contract = new web3.eth.Contract(
      JSON.parse(abi),
      addressTo
    );
    
    const DEADLINE = 1742680400 // deadline = w3.eth.getBlock(w3.eth.blockNumber).timestamp
    const ETH_ADDED = web3.utils.toHex(1*10**17) // 0.1 ETH
    const TOKEN_ADDED = web3.utils.toHex(15*10**18) // 15  tokens
    const tx = contract.methods.addLiquidity(1, TOKEN_ADDED, DEADLINE);
    const encodedABI = tx.encodeABI();
    
    // Signs the given transaction data and sends it.
    function sendSigned(txData, cb) {
      const privateKey = new Buffer(privKey, 'hex')
      const transaction = new Tx(txData)
      transaction.sign(privateKey)
      const serializedTx = transaction.serialize().toString('hex')
      web3.eth.sendSignedTransaction('0x' + serializedTx, cb)
    }
    
    // get the number of transactions sent so far so we can create a fresh nonce
    web3.eth.getTransactionCount(addressFrom).then(txCount => {
      // construct the transaction data
      const txData = {
        nonce: web3.utils.toHex(txCount),
        gasLimit: web3.utils.toHex(6000000),
        gasPrice: web3.utils.toHex(10000000000), // 10 Gwei
        to: addressTo,
        from: addressFrom,
        data: encodedABI,
        value: ETH_ADDED
      }
      // fire away!
      sendSigned(txData, function(err, result) {
        if (err) return console.log('error', err)
        console.log('sent', result)
      })
    })
 */
//@formatter:on
// 5.eth2erc20.swap.js 
//@formatter:off
/*
    let Web3 = require("web3");
    const Tx = require('ethereumjs-tx')
    
    var abi = '[{"name": "TokenPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "eth_sold", "indexed": true}, {"type": "uint256", "name": "tokens_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "EthPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "tokens_sold", "indexed": true}, {"type": "uint256", "name": "eth_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "AddLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "RemoveLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "Transfer", "inputs": [{"type": "address", "name": "_from", "indexed": true}, {"type": "address", "name": "_to", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "Approval", "inputs": [{"type": "address", "name": "_owner", "indexed": true}, {"type": "address", "name": "_spender", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "setup", "outputs": [], "inputs": [{"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 175875}, {"name": "addLiquidity", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_liquidity"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 82605}, {"name": "removeLiquidity", "outputs": [{"type": "uint256", "name": "out"}, {"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "amount"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 116814}, {"name": "__default__", "outputs": [], "inputs": [], "constant": false, "payable": true, "type": "function"}, {"name": "ethToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 12757}, {"name": "ethToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 12965}, {"name": "ethToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 50463}, {"name": "ethToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 50671}, {"name": "tokenToEthSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 47503}, {"name": "tokenToEthTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 47712}, {"name": "tokenToEthSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 50175}, {"name": "tokenToEthTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 50384}, {"name": "tokenToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51007}, {"name": "tokenToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51098}, {"name": "tokenToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 54928}, {"name": "tokenToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 55019}, {"name": "tokenToExchangeSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49342}, {"name": "tokenToExchangeTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49532}, {"name": "tokenToExchangeSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53233}, {"name": "tokenToExchangeTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53423}, {"name": "getEthToTokenInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5542}, {"name": "getEthToTokenOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6872}, {"name": "getTokenToEthInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5637}, {"name": "getTokenToEthOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6897}, {"name": "tokenAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1413}, {"name": "factoryAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1443}, {"name": "balanceOf", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}], "constant": true, "payable": false, "type": "function", "gas": 1645}, {"name": "transfer", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 75034}, {"name": "transferFrom", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_from"}, {"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 110907}, {"name": "approve", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_spender"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 38769}, {"name": "allowance", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}, {"type": "address", "name": "_spender"}], "constant": true, "payable": false, "type": "function", "gas": 1925}, {"name": "name", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1623}, {"name": "symbol", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1653}, {"name": "decimals", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1683}, {"name": "totalSupply", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1713}]'
    
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    // the address that will send the test transaction
    const addressFrom = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c'
    const privKey = process.env.privateKey
    // the exchange contract address
    const addressTo = '0x416F1Ac032D1eEE743b18296aB958743B1E61E81'
    const contract = new web3.eth.Contract(
      JSON.parse(abi),
      addressTo
    );
    // exchange smart contract: https://github.com/Uniswap/contracts-vyper/blob/master/contracts/uniswap_exchange.vy
    const DEADLINE = 1742680400
    const ETH_SOLD = web3.utils.toHex(5*10**16) // 0.1 ETH
    const tx = contract.methods.ethToTokenSwapInput(1, DEADLINE)
    const encodedABI = tx.encodeABI();
    
    // Signs the given transaction data and sends it.
    function sendSigned(txData, cb) {
      const privateKey = new Buffer(privKey, 'hex')
      const transaction = new Tx(txData)
      transaction.sign(privateKey)
      const serializedTx = transaction.serialize().toString('hex')
      web3.eth.sendSignedTransaction('0x' + serializedTx, cb)
    }
    
    // get the number of transactions sent so far so we can create a fresh nonce
    web3.eth.getTransactionCount(addressFrom).then(txCount => {
      // construct the transaction data
      const txData = {
        nonce: web3.utils.toHex(txCount),
        gasLimit: web3.utils.toHex(6000000),
        gasPrice: web3.utils.toHex(10000000000), // 10 Gwei
        to: addressTo,
        from: addressFrom,
        data: encodedABI,
        value: ETH_SOLD
      }
      // fire away!
      sendSigned(txData, function(err, result) {
        if (err) return console.log('error', err)
        console.log('sent', result)
      })
    })
 */
//@formatter:on
// 6.convert.token2token.js
//@formatter:off
/*
    let Web3 = require("web3");
    const Tx = require('ethereumjs-tx')
    
    var abi = '[{"name": "TokenPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "eth_sold", "indexed": true}, {"type": "uint256", "name": "tokens_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "EthPurchase", "inputs": [{"type": "address", "name": "buyer", "indexed": true}, {"type": "uint256", "name": "tokens_sold", "indexed": true}, {"type": "uint256", "name": "eth_bought", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "AddLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "RemoveLiquidity", "inputs": [{"type": "address", "name": "provider", "indexed": true}, {"type": "uint256", "name": "eth_amount", "indexed": true}, {"type": "uint256", "name": "token_amount", "indexed": true}], "anonymous": false, "type": "event"}, {"name": "Transfer", "inputs": [{"type": "address", "name": "_from", "indexed": true}, {"type": "address", "name": "_to", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "Approval", "inputs": [{"type": "address", "name": "_owner", "indexed": true}, {"type": "address", "name": "_spender", "indexed": true}, {"type": "uint256", "name": "_value", "indexed": false}], "anonymous": false, "type": "event"}, {"name": "setup", "outputs": [], "inputs": [{"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 175875}, {"name": "addLiquidity", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_liquidity"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 82605}, {"name": "removeLiquidity", "outputs": [{"type": "uint256", "name": "out"}, {"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "amount"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 116814}, {"name": "__default__", "outputs": [], "inputs": [], "constant": false, "payable": true, "type": "function"}, {"name": "ethToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 12757}, {"name": "ethToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "min_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 12965}, {"name": "ethToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": true, "type": "function", "gas": 50463}, {"name": "ethToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": true, "type": "function", "gas": 50671}, {"name": "tokenToEthSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 47503}, {"name": "tokenToEthTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_eth"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 47712}, {"name": "tokenToEthSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}], "constant": false, "payable": false, "type": "function", "gas": 50175}, {"name": "tokenToEthTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}, {"type": "uint256", "name": "max_tokens"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}], "constant": false, "payable": false, "type": "function", "gas": 50384}, {"name": "tokenToTokenSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51007}, {"name": "tokenToTokenTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 51098}, {"name": "tokenToTokenSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 54928}, {"name": "tokenToTokenTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "token_addr"}], "constant": false, "payable": false, "type": "function", "gas": 55019}, {"name": "tokenToExchangeSwapInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49342}, {"name": "tokenToExchangeTransferInput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}, {"type": "uint256", "name": "min_tokens_bought"}, {"type": "uint256", "name": "min_eth_bought"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 49532}, {"name": "tokenToExchangeSwapOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53233}, {"name": "tokenToExchangeTransferOutput", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}, {"type": "uint256", "name": "max_tokens_sold"}, {"type": "uint256", "name": "max_eth_sold"}, {"type": "uint256", "name": "deadline"}, {"type": "address", "name": "recipient"}, {"type": "address", "name": "exchange_addr"}], "constant": false, "payable": false, "type": "function", "gas": 53423}, {"name": "getEthToTokenInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5542}, {"name": "getEthToTokenOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6872}, {"name": "getTokenToEthInputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "tokens_sold"}], "constant": true, "payable": false, "type": "function", "gas": 5637}, {"name": "getTokenToEthOutputPrice", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "uint256", "name": "eth_bought"}], "constant": true, "payable": false, "type": "function", "gas": 6897}, {"name": "tokenAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1413}, {"name": "factoryAddress", "outputs": [{"type": "address", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1443}, {"name": "balanceOf", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}], "constant": true, "payable": false, "type": "function", "gas": 1645}, {"name": "transfer", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 75034}, {"name": "transferFrom", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_from"}, {"type": "address", "name": "_to"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 110907}, {"name": "approve", "outputs": [{"type": "bool", "name": "out"}], "inputs": [{"type": "address", "name": "_spender"}, {"type": "uint256", "name": "_value"}], "constant": false, "payable": false, "type": "function", "gas": 38769}, {"name": "allowance", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [{"type": "address", "name": "_owner"}, {"type": "address", "name": "_spender"}], "constant": true, "payable": false, "type": "function", "gas": 1925}, {"name": "name", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1623}, {"name": "symbol", "outputs": [{"type": "bytes32", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1653}, {"name": "decimals", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1683}, {"name": "totalSupply", "outputs": [{"type": "uint256", "name": "out"}], "inputs": [], "constant": true, "payable": false, "type": "function", "gas": 1713}]'
    
    let web3 = new Web3(new Web3.providers.HttpProvider("https://rinkeby.infura.io/"));
    // the address that will send the test transaction
    const addressFrom = '0x0e364eb0ad6eb5a4fc30fc3d2c2ae8ebe75f245c'
    const privKey = process.env.privateKey
    // the exchange contract address
    const addressTo = '0xaFD52EF3Cb0eE6673cA5EbE0A25686313fF0C283'
    const linkTokenAddress ='0x01BE23585060835E02B77ef475b0Cc51aA1e0709'
    const contract = new web3.eth.Contract(
      JSON.parse(abi),
      addressTo
    );
    // exchange smart contract: https://github.com/Uniswap/contracts-vyper/blob/master/contracts/uniswap_exchange.vy
    const DEADLINE = 1742680400
    const LINK_BOUGHT = web3.utils.toHex(10*10**18) // 10 link tokens
    const MAX_TOKEN_SOLD = web3.utils.toHex(80*10**18) // 10 link tokens
    const MAX_ETH_SOLD = web3.utils.toHex(1*10**17) // 0.1 ETH
    
    const tx = contract.methods.tokenToTokenTransferOutput(LINK_BOUGHT, MAX_TOKEN_SOLD, MAX_ETH_SOLD, DEADLINE, addressFrom, linkTokenAddress)
    const encodedABI = tx.encodeABI();
    
    // Signs the given transaction data and sends it.
    function sendSigned(txData, cb) {
      const privateKey = new Buffer(privKey, 'hex')
      const transaction = new Tx(txData)
      transaction.sign(privateKey)
      const serializedTx = transaction.serialize().toString('hex')
      web3.eth.sendSignedTransaction('0x' + serializedTx, cb)
    }
    
    // get the number of transactions sent so far so we can create a fresh nonce
    web3.eth.getTransactionCount(addressFrom).then(txCount => {
      // construct the transaction data
      const txData = {
        nonce: web3.utils.toHex(txCount),
        gasLimit: web3.utils.toHex(6000000),
        gasPrice: web3.utils.toHex(10000000000), // 10 Gwei
        to: addressTo,
        from: addressFrom,
        data: encodedABI
      }
      // fire away!
      sendSigned(txData, function(err, result) {
        if (err) return console.log('error', err)
        console.log('sent', result)
      })
    })
 */
//@formatter:on
@RestController
@RequestMapping("/univ1")
public class ETHUniSwapV1Restful {

    @Autowired
    private Web3JFactory web3JFactory;

    // https://www.coingecko.com/en/exchanges/uniswap
    // 列出所有代币交换合约地址
    @RequestMapping(value = "/getPairAddr", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<List<String>> exchanges() {
        List<String> result = new ArrayList<>();
        return R.ok(result);
    }

    /*
        // uniswap v1 工厂合约地址
        const mainnet = '0xc0a47dFe034B400B47bDaD5FecDa2621de6c4d95'
        const ropsten = '0x9c83dCE8CA20E9aAF9D3efc003b2ea62aBC08351'
        const rinkeby = '0xf5D915570BC477f9B8D6C0E980aA81757A3AaC36'
        const kovan = '0xD3E51Ef092B2845f10401a0159B2B96e8B6c3D30'
        const görli = '0x6Ce570d02D73d4c384b46135E87f8C592A8c86dA'
     */
    // 查询代币交换合约地址
    @RequestMapping(value = "/getExchangeAddr", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getExchangeAddr(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "uniswapV1FactoryAddr", required = false, defaultValue = "0xc0a47dFe034B400B47bDaD5FecDa2621de6c4d95") String uniswapV1FactoryAddr,
            @RequestParam(value = "tokenAddr", required = true) String tokenAddr
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(tokenAddr).withFailMessage("`tokenAddr` 必传").isNotBlank();
        String exchangeAddr = new RemoteCallFunction<>(new UniswapV1FactoryContract(uniswapV1FactoryAddr, web3JFactory.get(ChainId.get(chainId))).getExchangeAddr(tokenAddr)).call();
        return R.ok(Constrants.EMPTY_ADDRESS.equalsIgnoreCase(exchangeAddr) ? null : exchangeAddr);
    }

    // 根据交换合约地址查询代币地址
    @RequestMapping(value = "/getTokenAddress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getTokenAddress(
            @RequestParam(value = "chainId", required = false, defaultValue = "mainnet") String chainId, // 网络类型: 主网或测试网
            @RequestParam(value = "uniswapV1FactoryAddr", required = false, defaultValue = "0xc0a47dFe034B400B47bDaD5FecDa2621de6c4d95") String uniswapV1FactoryAddr,
            @RequestParam(value = "exchangeAddr", required = true) String exchangeAddr
    ) {
        Assertions.assertThat(ChainId.get(chainId)).withFailMessage("%s 有误", "chainId").isNotNull();
        Assertions.assertThat(exchangeAddr).withFailMessage("`exchangeAddr` 必传").isNotBlank();
        String tokenAddr = new RemoteCallFunction<>(new UniswapV1FactoryContract(uniswapV1FactoryAddr, web3JFactory.get(ChainId.get(chainId))).getToken(exchangeAddr)).call();
        return R.ok(Constrants.EMPTY_ADDRESS.equalsIgnoreCase(tokenAddr) ? null : tokenAddr);
    }
    
}
