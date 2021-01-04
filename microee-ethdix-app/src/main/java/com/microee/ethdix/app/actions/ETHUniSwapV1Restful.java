package com.microee.ethdix.app.actions;

import com.microee.ethdix.j3.rpc.JsonRPC;
import com.microee.plugin.response.R;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/uniswapv1")
public class ETHUniSwapV1Restful {
    
    @Autowired
    @Qualifier("jsonRPCClientMainnet")
    private JsonRPC jsonRPCClientMainnet;

    @Autowired
    @Qualifier("jsonRPCClientRopsten")
    private JsonRPC jsonRPCClientRopsten;

    // 根据网络类型随机返回一个节点地址
    @RequestMapping(value = "/getETHNode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public R<String> getETHNode(
            @RequestParam(value = "network", required = false, defaultValue = "mainnet") String network // 网络类型: 主网或测试网
    ) {
        Assertions.assertThat(network).withFailMessage("`network` 必传").isNotBlank();
        if (network != null && !network.isEmpty() && network.equals("mainnet")) {
            return R.ok(jsonRPCClientMainnet.getEthNode());
        }
        return R.ok(jsonRPCClientRopsten.getEthNode());
    }
}
