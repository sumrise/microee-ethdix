import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import { ChainId, Route, TokenAmount, TradeType, Trade, WETH, Percent } from '@uniswap/sdk';
import { parseUnits } from '@ethersproject/units';
import web3 from 'web3';

import { getTokenDataByAddr, getPairDataWithSymbol } from '../../univ2-resolves/UniV2Fetcher';

const loggerInfo: debug.IDebugger = debug('app-univ2-trade');

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class Univ2SwapParamsRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'Univ2SwapParamsRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-swap-params`)
            .get((req: express.Request, res: express.Response) => {
                res.status(200).send(`UniSwapV2 Trading Page.`);
            });
        // 获取 UniSwapV2 ETH to Token 兑换参数数据
        this.app.route(`/univ2-swap-params/eth2token`)
            .post((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _ethInputAmount: string = req.body.ethInputAmount as string;
                const _slippageToleranceInput: number = req.body.slippageTolerance as number; // new Percent('50', '10000') 20:10000=0.2=20%, 50:10000=0.5=50%, 100:10000=1=100%
                expect(web3.utils.isAddress(_tokenAddr), '_tokenAddr not invalid').to.be.true;
                expect(_ethInputAmount, 'ethInputAmount should >0 and to be number').to.satisfy((num: string) => num.match(/^\d+(\.\d+)?$/) && num !== '0');
                expect(_slippageToleranceInput, 'slippageTolerance should >0 and < 100').to.be.within(1, 100);
                const _wethAddr = WETH[_chainId];
                (async () => {
                    try {
                        const token = await getTokenDataByAddr(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                        const pairWithSymbol = await getPairDataWithSymbol(_chainId, token, _wethAddr);
                        const pair = pairWithSymbol[0];
                        const symbol = pairWithSymbol[1];
                        const route = new Route([pair], _wethAddr);
                        const _tokenAmountString = parseUnits(_ethInputAmount, _wethAddr.decimals).toString();
                        const trade = new Trade(route, new TokenAmount(_wethAddr, _tokenAmountString), TradeType.EXACT_INPUT);
                        const _slippageTolerance = new Percent(_slippageToleranceInput.toString(), '10000');
                        const tradInfo = {
                            amountOutMin: trade.minimumAmountOut(_slippageTolerance).toSignificant(6),
                            path: [_wethAddr.address, token.address],
                            to: '',
                            value: trade.inputAmount.toSignificant(6)
                        };
                        const message = `兑换-ETH2TokenSwap: 
                                        chainId=${_chainId},
                                        slippageTolerance=${_slippageTolerance.toSignificant(6)}, 
                                        amountOutMin=${tradInfo.amountOutMin}, 
                                        symbol=${symbol},
                                        path:[${route.path[0].address}, ${route.path[1].address}],
                                        to: ${tradInfo.to},
                                        valueAmount: ${tradInfo.value},
                                        valueJsbiBigInt: ${_tokenAmountString},
                                        liquidityToken=${pair.liquidityToken.symbol}, 
                                        route.midPrice=${route.midPrice.toSignificant(6)}, 
                                        route.midPrice.invert=${route.midPrice.invert().toSignificant(6)},
                                        trade.executionPrice=${trade.executionPrice.toSignificant(6)},
                                        trade.nextMidPrice=${trade.nextMidPrice.toSignificant(6)}`.replace(/(\r\n|\n|\r)/gm, "").replace(/(\s+)/gm, " ");
                        loggerInfo(message);
                        const tradInfoVo = Object.assign(tradInfo);
                        res.status(200).json({ code: 200, message: message, data: tradInfoVo });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        return this.app;
    }
}