import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import { ChainId, Route, Token, TokenAmount, TradeType, Trade, WETH, Percent, JSBI, CurrencyAmount } from '@uniswap/sdk';
import { ethers } from 'ethers';
import { parseUnits } from '@ethersproject/units';
import web3 from 'web3';

import { getTokenDataByAddr, getPairDataWithSymbol } from '../../univ2-resolves/UniV2Fetcher';

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
        // 根据输入的数量返回代币的最小单位表示形式
        this.app.route(`/univ2-trade/jsbiBigInt`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _amount: string = req.query['amount'] as string; // 代币数量
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                expect(_amount, '_amount invalid').to.length.gt(0);
                (async () => {
                    try {
                        const _token: Token = await getTokenDataByAddr(_chainId, _tokenAddr);
                        const typedValueParsed = parseUnits(_amount, _token.decimals).toString();
                        expect(typedValueParsed !== '0', 'amount must > 0').to.be.true;
                        const _currencyAmount = new TokenAmount(_token, JSBI.BigInt(typedValueParsed));
                        res.status(200).json({ code: 200, message: _currencyAmount.toSignificant(6), data: typedValueParsed });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 获取 UniSwapV2 兑换参数数据
        this.app.route(`/univ2-trade/eth2TokenSwapGetParams`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _debugger: boolean = req.query['debug'] ? true : false; // 代币地址
                const _params = Object.assign({}, req.body);
                loggerInfo(`_params: ${JSON.stringify(_params)}`);
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                const _wethAddr = WETH[_chainId];
                (async () => {
                    try {
                        const token = await getTokenDataByAddr(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                        const pairWithSymbol = await getPairDataWithSymbol(_chainId, token, _wethAddr);
                        const pair = pairWithSymbol[0];
                        const route = new Route([pair], _wethAddr);
                        const trade = new Trade(route, new TokenAmount(_wethAddr, '100000000000000000'), TradeType.EXACT_INPUT);
                        const _slippageTolerance = new Percent('50', '10000'); // 50 bips 1 bip = 0.05
                        const tradInfo = {
                            slippageTolerance: _slippageTolerance,
                            amountOutMin: trade.minimumAmountOut(_slippageTolerance),
                            // amountOutMin: trade.minimumAmountOut(_slippageTolerance).raw,
                            path: [_wethAddr.address, token.address],
                            to: '',
                            deadline: Math.floor(new Date().getTime() / 1000) + 60 * 20, // 20 分钟
                            value: trade.inputAmount,
                            // value: trade.inputAmount.raw
                        };
                        const message = `兑换-ETH2TokenSwap: 
                                        chainId=${_chainId},
                                        slippageTolerance=${_slippageTolerance.toSignificant(6)}, 
                                        amountOutMin=${tradInfo.amountOutMin.toSignificant(6)}, 
                                        path:[${route.path[0].symbol || route.path[0].address}, ${route.path[1].symbol || route.path[1].address}],
                                        to: ${tradInfo.to},
                                        deadline: ${tradInfo.deadline},
                                        value: ${tradInfo.value.toSignificant(6)},
                                        liquidityToken=${pair.liquidityToken.symbol}, 
                                        route.midPrice=${route.midPrice.toSignificant(6)}, 
                                        route.midPrice.invert=${route.midPrice.invert().toSignificant(6)},
                                        trade.executionPrice=${trade.executionPrice.toSignificant(6)},
                                        trade.nextMidPrice=${trade.nextMidPrice.toSignificant(6)}`.replace(/(\r\n|\n|\r)/gm, "").replace(/(\s+)/gm, " ");
                        loggerInfo(message);
                        const tradInfoVo = Object.assign(tradInfo, !_debugger ? null :
                            {
                                slippageTolerance: tradInfo.slippageTolerance.toSignificant(6),
                                amountOutMin: tradInfo.amountOutMin.toSignificant(6),
                                value: tradInfo.value.toSignificant(6),
                                debuger: { pair: pair, route: route, trade: trade }
                            });
                        res.status(200).json({ code: 200, message: message, data: tradInfoVo });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 发送eth2token兑换
        this.app.route(`/univ2-trade/eth2TokenSwapSendTranaction`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _swapExactETHForTokensABI = 'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts);';
                // header params
                const _privateKey: string = req.headers['private-key'] as string;
                // query params
                const _ethnode: string = req.query['ethnode'] as string;
                const _router02Addr: string = req.query['router02Addr'] as string;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                // body params
                const _amountOutMin: string = req.body.amountOutMin as string;
                const _ethInputAmount: string = req.body.ethInputAmount as string;
                const _path: string[] = req.body.path as string[];
                const _gas: number = req.body.gas as number; // 不是最小单位
                const _deadline: number = req.body.deadline as number; // 单位是秒
                // validator
                expect(_ethnode, 'ethnode 无效').to.length.gte(42);
                expect(_router02Addr, 'router02Addr 无效').to.have.lengthOf(42);
                expect(_privateKey, 'privateKey 无效').to.length.gte(42);
                expect(_amountOutMin, 'amountOutMin shoud not be empty').to.length.gt(0);
                expect(_amountOutMin, 'amountOutMin shoud not equals 0').to.be.not.equal('0');
                expect(_ethInputAmount, 'ethInputAmount shoud not be empty').to.length.gt(0);
                expect(_ethInputAmount, 'ethInputAmount shoud not equals 0').to.not.equal('0');
                expect(_path, 'path shoud length of 2').to.have.lengthOf(2);
                expect(_gas, 'gas not invalid').to.be.gt(1);
                expect(web3.utils.isAddress(_router02Addr), 'router02Addr not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenAddr), 'tokenAddr not invalid').to.be.true;
                expect(web3.utils.isAddress(_path[0]), 'path[0] not invalid').to.be.true;
                expect(web3.utils.isAddress(_path[1]), 'path[1] not invalid').to.be.true;
                (async () => {
                    try {
                        const _token: Token = await getTokenDataByAddr(_chainId, _tokenAddr);
                        const amountOutMin: CurrencyAmount = new TokenAmount(_token, JSBI.BigInt(parseUnits(_amountOutMin, _token.decimals).toString()));
                        const ethInputAmount: CurrencyAmount = CurrencyAmount.ether(JSBI.BigInt(parseUnits(_ethInputAmount, _token.decimals).toString()));
                        const _gasPrice = parseUnits(_gas.toString(), 9).toString();
                        loggerInfo(`_tradInfo: amountOutMin=${amountOutMin.toSignificant(6)}, path=${_path}, deadline=${_deadline}, value=${ethInputAmount.toSignificant(6)}, gasPrice=${_gasPrice}`);
                        const _account = new ethers.Wallet(_privateKey).connect(ethers.getDefaultProvider('mainnet', { infura: _ethnode }));
                        const _uniswapv2 = new ethers.Contract(_router02Addr, [_swapExactETHForTokensABI], _account);
                        const _calldata: any = { ethInputAmount, gasPrice: JSBI.BigInt(_gasPrice) };
                        const tx = await _uniswapv2.sendExactETHForTokens(amountOutMin, _path, '', _deadline, _calldata);
                        loggerInfo(`Transaction Hash: ${tx.hash}`);
                        const recepit = await tx.wait();
                        loggerInfo(`Transaction was mined in block: ${recepit.blockNumber}`);
                        res.status(200).json(recepit);
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        return this.app;
    }
}