import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';

import { ChainId, Fetcher, Route, Pair, TokenAmount, TradeType, Trade, WETH, Percent } from '@uniswap/sdk'
import { ethers } from 'ethers';

const loggerInfo: debug.IDebugger = debug('app-univ2-trade');

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2TradeRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2TradeRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-trade`)
            .get((req: express.Request, res: express.Response) => {
                res.status(200).send(`UniSwapV2 Trading Page.`);
            });
        // 获取 UniSwapV2 兑换参数数据
        this.app.route(`/univ2-trade/eth2TokenSwapGetParams`)
            .post((req: express.Request, res: express.Response) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _debugger: boolean = req.query['debug'] ? true : false; // 代币地址
                const _params = Object.assign({}, req.body);
                loggerInfo(`_params: ${JSON.stringify(_params)}`);
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                const _wethAddr = WETH[_chainId];
                (async () => {
                    const token = await Fetcher.fetchTokenData(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                    const pair = await Fetcher.fetchPairData(token, _wethAddr);
                    const route = new Route([pair], _wethAddr);
                    const trade = new Trade(route, new TokenAmount(_wethAddr, '100000000000000000'), TradeType.EXACT_INPUT);
                    const _slippageTolerance = new Percent('50', '10000'); // 50 bips 1 bip = 0.05
                    const tradInfo = {
                        slippageTolerance: _slippageTolerance,
                        amountOutMin: trade.minimumAmountOut(_slippageTolerance).raw,
                        path: [_wethAddr.address, token.address],
                        to: '',
                        deadline: Math.floor(new Date().getTime() / 1000) + 60 * 20, // 20 分钟
                        value: trade.inputAmount.raw
                    };
                    const message = `兑换-ETH2TokenSwap: 
                                    slippageTolerance=${_slippageTolerance.toSignificant(6)}, 
                                    amountOutMin=${tradInfo.amountOutMin}, 
                                    path:[${route.path[0].symbol || route.path[0].address}, ${route.path[1].symbol || route.path[1].address}],
                                    to: ${tradInfo.to},
                                    deadline: ${tradInfo.deadline},
                                    value: ${tradInfo.value},
                                    liquidityToken=${pair.liquidityToken.symbol}, 
                                    route.midPrice=${route.midPrice.toSignificant(6)}, 
                                    route.midPrice.invert=${route.midPrice.invert().toSignificant(6)},
                                    trade.executionPrice=${trade.executionPrice.toSignificant(6)},
                                    trade.nextMidPrice=${trade.nextMidPrice.toSignificant(6)}`.replace(/(\r\n|\n|\r)/gm, "").replace(/(\s+)/gm, " ");
                    loggerInfo(message);
                    const tradInfoVo = Object.assign(tradInfo, !_debugger ? null : { slippageTolerance: tradInfo.slippageTolerance.toSignificant(6), debuger: { pair: pair, route: route, trade: trade } });
                    res.status(200).json({ code: 200, message: message, data: tradInfoVo });
                })();
            });
        this.app.route(`/univ2-trade/eth2TokenSwapSendTranaction`)
            .post((req: express.Request, res: express.Response) => {
                const _swapExactETHForTokensABI = 'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts);';
                const _ethnode: string = req.query['ethnode'] as string;
                const _router02Addr: string = req.query['router02Addr'] as string;
                const _tradInfo = Object.assign({ privateKey: null, slippageTolerance: null, amountOutMin: null, path: [], to: '', deadline: null, value: null }, req.body);
                const _privateKey = _tradInfo.privateKey;
                loggerInfo(`_tradInfo: ${JSON.stringify(Object.assign(_tradInfo, { privateKey2: null }))}`);
                expect(_ethnode, 'ethnode invalid').to.length.gte(42);
                expect(_router02Addr, 'router02Addr invalid').to.have.lengthOf(42);
                expect(_privateKey, 'privateKey invalid').to.length.gte(42);
                (async () => {
                    const _value = _tradInfo.value;
                    const account = new ethers.Wallet(_tradInfo.privateKey).connect(ethers.getDefaultProvider('mainnet', { infura: _ethnode }));
                    const uniswapv2 = new ethers.Contract(_router02Addr, [_swapExactETHForTokensABI], account);
                    const tx = await uniswapv2.sendExactETHForTokens(_tradInfo.amountOutMin, _tradInfo.path, _tradInfo.to, _tradInfo.deadline, { _value, gasPrice: 20e9 });
                    loggerInfo(`Transaction Hash: ${tx.hash}`);
                    const recepit = await tx.wait();
                    loggerInfo(`Transaction was mined in block: ${recepit.blockNumber}`);
                    res.status(200).json(recepit);
                })();
            });
        return this.app;
    }
}