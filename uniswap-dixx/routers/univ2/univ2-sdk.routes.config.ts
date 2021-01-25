import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { expect } from 'chai';
import debug from 'debug';
import {ChainId, Route, Pair, TokenAmount, TradeType, Trade, Price, Token, JSBI} from '@uniswap/sdk';
import { getPairDataWithSymbol, getTokenDataWithPairSymbol, getTokenDataByAddress, getTokenDataByAddr, getPairSymbolByAddress, toTokenAmount } from '../../univ2-resolves/UniV2Fetcher';
import web3 from "web3";

const loggerInfo: debug.IDebugger = debug('app-univ2-sdk');

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2SDKRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2SDKRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2-sdk/token`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                expect(web3.utils.isAddress(_tokenAddr), 'tokenAddr not invalid').to.be.true;
                (async () => {
                    try {
                        const token = await getTokenDataByAddr(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                        res.status(200).json({ code: 200, message: 'OK', data: token });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        this.app.route(`/univ2-sdk/pair`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _method: string = req.query['method'] as string; // 想执行的方法
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                (async () => {
                    try {
                        const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                        const [ thePair, pairSymbol ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                        const result: any = { code: 200, message: `${pairSymbol}`, data: null };
                        if (_method === 'reserve0') {
                            Object.assign(result, { data: { reserve0: thePair.reserve0.toSignificant(6) } });
                        } else if (_method === 'reserve1') {
                            Object.assign(result, { data: { reserve1: thePair.reserve1.toSignificant(6) } });
                        } else if (_method === 'token0Price') {
                            Object.assign(result, { data: { token0Price: thePair.token0Price.toSignificant(6) } });
                        } else if (_method === 'token1Price') {
                            Object.assign(result, { data: { token1Price: thePair.token1Price.toSignificant(6) } });
                        } else if (_method === 'token0') {
                            Object.assign(result, { data: { token0: thePair.token0 } });
                        } else if (_method === 'token1') {
                            Object.assign(result, { data: { token1: thePair.token1 } });
                        } else {
                            Object.assign(result, { data: thePair });
                        }
                        res.status(200).json(result);
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        this.app.route(`/univ2-sdk/pair/getPairAddress`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                (async () => {
                    try {
                        const [ tokenA, tokenB, pairSymbol ] = await getTokenDataWithPairSymbol(_chainId, _tokenA, _tokenB);
                        res.status(200).json({ code: 200, message: `${pairSymbol}`, data: Pair.getAddress(tokenA, tokenB) });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 查询当前交易对兑换价格
        this.app.route(`/univ2-sdk/pair/priceOf`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _of: string = req.query['of'] as string; // 当前查哪个token的价格
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                expect(_of, 'of 无效').to.oneOf(['tokenA', 'tokenB']);
                (async () => {
                    // const address = Pair.getAddress(tokenA, tokenB)
                    // const [reserves0, reserves1] = await new Contract(address, IUniswapV2Pair.abi, provider).getReserves()
                    // const balances = tokenA.sortsBefore(tokenB) ? [reserves0, reserves1] : [reserves1, reserves0]
                    // new Pair(new TokenAmount(tokenA, balances[0]), new TokenAmount(tokenB, balances[1]))
                    try {
                        const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                        loggerInfo(`${tokenA.address}-${tokenB.address}`);
                        const [ thePair ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                        const thePrice: Price = thePair.priceOf(_of === 'tokenA' ? tokenA : tokenB);
                        // hbtc/usdt 可以需要迂回交易
                        // TODO
                        const baseCurrencyObject: any = JSON.parse(JSON.stringify(thePrice.baseCurrency));
                        const quoteCurrencyObject: any = JSON.parse(JSON.stringify(thePrice.quoteCurrency));
                        const [baseSymbol, quoteSymbol] = getPairSymbolByAddress(_chainId, baseCurrencyObject.address, quoteCurrencyObject.address);
                        return res.status(200).json({ code: 200, message: `${baseSymbol}/${quoteSymbol}`, data: thePrice.toSignificant(6) });
                    } catch (err) {
                        next(err);
                    }
                })();
            });

        // 确切输入
        // 如果您想发送确切数量的输入令牌以换取尽可能多的输出令牌，则需要使用 getAmountsOut。
        // 等效的SDK函数为 getOutputAmount 或 minimumAmountOut 用于滑点计算。
        // 根据输入数量计算输出数量
        this.app.route(`/univ2-sdk/pair/getOutputAmount`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _tokenAInputAmount: string = req.query['tokenAInputAmount'] as string; // 代币A的数量
                const _tokenBInputAmount: string = req.query['tokenBInputAmount'] as string; // 代币B的数量
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                expect(typeof _tokenAInputAmount !== 'undefined' || typeof _tokenBInputAmount !== 'undefined', 'tokenAInputAmount OR tokenBInputAmount 二传一').to.be.true;
                if (typeof _tokenAInputAmount !== 'undefined') expect(_tokenAInputAmount.trim(), 'tokenAInputAmount 无效').to.length.gt(0);
                if (typeof _tokenBInputAmount !== 'undefined') expect(_tokenBInputAmount.trim(), 'tokenBInputAmount 无效').to.length.gt(0);
                (async () => {
                    const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                    const [ thePair ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                    try {
                        let amountResult = null;
                        if (_tokenAInputAmount != null) {
                            amountResult = thePair.getOutputAmount(await toTokenAmount(_chainId, _tokenA, _tokenAInputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBInputAmount != null) {
                            amountResult = thePair.getOutputAmount(await toTokenAmount(_chainId, _tokenB, _tokenBInputAmount))[0].toSignificant(6);
                        }
                        return res.status(200).json({ code: 200, message: 'OK', data: amountResult });
                    } catch (err) {
                        next(err);
                    }
                })();
            });

        // 精确输出
        // 如果您希望为尽可能少的输入令牌接收确切数量的输出令牌，则可以使用 getAmountsIn。
        // 等效的 SDK 函数为 getInputAmount 或 maximumAmountIn，用于滑点计算。
        // 根据输出数量计算输入数量
        this.app.route(`/univ2-sdk/pair/getInputAmount`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _tokenAOutputAmount: string = req.query['tokenAOutputAmount'] as string; // 代币A的数量
                const _tokenBOutputAmount: string = req.query['tokenBOutputAmount'] as string; // 代币B的数量
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                expect(typeof _tokenAOutputAmount !== 'undefined' || typeof _tokenBOutputAmount !== 'undefined', 'tokenAOutputAmount OR tokenBOutputAmount 二传一').to.be.true;
                if (typeof _tokenAOutputAmount !== 'undefined') expect(_tokenAOutputAmount.trim(), 'tokenAOutputAmount 无效').to.length.gt(0);
                if (typeof _tokenBOutputAmount !== 'undefined') expect(_tokenBOutputAmount.trim(), 'tokenBOutputAmount 无效').to.length.gt(0);
                (async () => {
                    const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                    const [ thePair ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                    try {
                        let amountResult = null;
                        if (_tokenAOutputAmount != null) {
                            amountResult = thePair.getInputAmount(await toTokenAmount(_chainId, _tokenA, _tokenAOutputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBOutputAmount != null) {
                            amountResult = thePair.getInputAmount(await toTokenAmount(_chainId, _tokenB, _tokenBOutputAmount))[0].toSignificant(6);
                        }
                        return res.status(200).json({ code: 200, message: 'OK', data: amountResult });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        this.app.route(`/univ2-sdk/route`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                (async () => {
                    const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                    const [ thePair ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                    const route = new Route([thePair], tokenB);
                    let result = { code: 200, message: 'OK', data: route };
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2-sdk/trade`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                expect(web3.utils.isAddress(_tokenA), 'tokenA not invalid').to.be.true;
                expect(web3.utils.isAddress(_tokenB), 'tokenB not invalid').to.be.true;
                (async () => {
                    const [ tokenA, tokenB ] = await getTokenDataByAddress(_chainId, _tokenA, _tokenB);
                    const [ thePair ]: [Pair, string] = await getPairDataWithSymbol(_chainId, tokenA, tokenB);
                    const route = new Route([thePair], tokenB);
                    const trade = new Trade(route, new TokenAmount(tokenB, '1000000000000000'), TradeType.EXACT_INPUT);
                    let result = { code: 200, message: 'OK', data: trade };
                    res.status(200).json(result);
                })();
            });
        return this.app;
    }
}