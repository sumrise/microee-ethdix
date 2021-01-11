import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';

import { ChainId, Fetcher, Route, Pair, TokenAmount, TradeType, Trade, WETH, Percent } from '@uniswap/sdk'

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
        this.app.route(`/univ2-trade/eth2TokenSwap`)
            .post((req: express.Request, res: express.Response) => {
                const _chainId = ChainId.MAINNET;
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _slippageTolerance = new Percent('50', '10000'); // 50 bips 1 bip = 0.05
                const _params = Object.assign({}, req.body);
                loggerInfo(`_params: ${JSON.stringify(_params)}`);
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                const _wethAddr = WETH[_chainId];
                (async () => {
                    const token = await Fetcher.fetchTokenData(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                    const pair = await Fetcher.fetchPairData(token, _wethAddr);
                    const route = new Route([pair], _wethAddr);
                    const trade = new Trade(route, new TokenAmount(_wethAddr, '100000000000000000'), TradeType.EXACT_INPUT);
                    const amountOutMin = trade.minimumAmountOut(_slippageTolerance).raw;
                    const path = [_wethAddr.address, token.address];
                    const to = '';
                    const deadline = Math.floor(new Date().getTime() / 1000) + 60 * 20;
                    const message = `兑换-ETH2TokenSwap: 
                                    slippageTolerance=${_slippageTolerance.toSignificant(6)}, 
                                    amountOutMin=${amountOutMin}, 
                                    liquidityToken=${pair.liquidityToken.symbol}, 
                                    path:[${route.path[0].symbol || route.path[0].address}, ${route.path[1].symbol || route.path[1].address}],
                                    route.midPrice=${route.midPrice.toSignificant(6)}, 
                                    route.midPrice.invert=${route.midPrice.invert().toSignificant(6)},
                                    trade.executionPrice=${trade.executionPrice.toSignificant(6)},
                                    trade.nextMidPrice=${trade.nextMidPrice.toSignificant(6)}`.replace(/(\r\n|\n|\r)/gm, "").replace(/(\s+)/gm, " ");
                    loggerInfo(message);
                    let result = {
                        code: 200,
                        message: message,
                        data: {
                            slippageTolerance: _slippageTolerance.toSignificant(6),
                            amountOutMin: amountOutMin,
                            path: path,
                            to: to,
                            deadline: deadline,
                            // debuger: {
                            //     pair: pair,
                            //     route: route,
                            //     trade: trade
                            // }
                        },
                    };
                    res.status(200).json(result);
                })();
            });
        return this.app;
    }
}