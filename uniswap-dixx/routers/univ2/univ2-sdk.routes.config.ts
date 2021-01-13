import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { getNetwork } from '@ethersproject/networks';
import { getDefaultProvider } from '@ethersproject/providers';
import { expect } from 'chai';
import { ChainId, Fetcher, Route, Pair, TokenAmount, TradeType, Trade, Price, Currency } from '@uniswap/sdk';
import { ethers } from 'ethers';
import { Eth } from 'web3-eth';

import { tokens as DefaultTokenList } from '@uniswap/default-token-list/build/uniswap-default.tokenlist.json';

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
                expect(_tokenAddr, 'tokenAddr 无效').to.have.lengthOf(42);
                (async () => {
                    const token = await Fetcher.fetchTokenData(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                    res.status(200).json({ code: 200, message: 'OK', data: token });
                })();
            });
        this.app.route(`/univ2-sdk/pair`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _method: string = req.query['method'] as string; // 想执行的方法
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    try {
                        const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                        const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                        // const provider = new ethers.providers.Web3Provider(window.ethereum);
                        // const thePair = await Fetcher.fetchPairData(tokenA, tokenB, provider);
                        const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
                        const token0: any = DefaultTokenList.find(t => t.chainId == _chainId && (thePair.token0.address.toLowerCase() === t.address.toLowerCase()));
                        const token1: any = DefaultTokenList.find(t => t.chainId == _chainId && (thePair.token1.address.toLowerCase() === t.address.toLowerCase()));
                        const token0Symbol: string = token0 ? token0.symbol : thePair.token0.address;
                        const token1Symbol: string = token1 ? token1.symbol : thePair.token1.address;
                        const result: any = { code: 200, message: `${token0Symbol}/${token1Symbol}`, data: null };
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
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    try {
                        const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                        const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                        const token0: any = DefaultTokenList.find(t => t.chainId == _chainId && (tokenA.address.toLowerCase() === t.address.toLowerCase()));
                        const token1: any = DefaultTokenList.find(t => t.chainId == _chainId && (tokenB.address.toLowerCase() === t.address.toLowerCase()));
                        const tokenPair = tokenA.sortsBefore(tokenB) ? [token0 ? token0.symbol : tokenA.address, token1 ? token1.symbol : tokenB.address] : [token1 ? token1.symbol : tokenB.address, token0 ? token0.symbol : tokenA.address];
                        res.status(200).json({ code: 200, message: `${tokenPair[0]}/${tokenPair[1]}`, data: Pair.getAddress(tokenA, tokenB) });
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
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                expect(_of, 'of 无效').to.oneOf(['tokenA', 'tokenB']);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
                    // const address = Pair.getAddress(tokenA, tokenB)
                    // const [reserves0, reserves1] = await new Contract(address, IUniswapV2Pair.abi, provider).getReserves()
                    // const balances = tokenA.sortsBefore(tokenB) ? [reserves0, reserves1] : [reserves1, reserves0]
                    // new Pair(new TokenAmount(tokenA, balances[0]), new TokenAmount(tokenB, balances[1]))
                    try {
                        const thePrice: Price = thePair.priceOf(_of === 'tokenA' ? tokenA : tokenB);
                        const baseCurrencyObject: any = JSON.parse(JSON.stringify(thePrice.baseCurrency));
                        const quoteCurrencyObject: any = JSON.parse(JSON.stringify(thePrice.quoteCurrency));
                        const baseCurrencyToken: any = DefaultTokenList.find(t => t.chainId == _chainId && (baseCurrencyObject.address.toLowerCase() === t.address.toLowerCase()));
                        const quoteCurrencyToken: any = DefaultTokenList.find(t => t.chainId == _chainId && (quoteCurrencyObject.address.toLowerCase() === t.address.toLowerCase()));
                        const baseCurrencyTokenSymbol: string = baseCurrencyToken ? baseCurrencyToken.symbol : baseCurrencyObject.address;
                        const quoteCurrencyTokenSymbol: string = quoteCurrencyToken ? quoteCurrencyToken.symbol : baseCurrencyObject.address;
                        return res.status(200).json({ code: 200, message: `${baseCurrencyTokenSymbol}/${quoteCurrencyTokenSymbol}`, data: thePrice.toSignificant(6) });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 根据输入数量计算输出数量
        this.app.route(`/univ2-sdk/pair/getOutputAmount`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _tokenAInputAmount: string = req.query['tokenAInputAmount'] as string; // 代币A的数量
                const _tokenBInputAmount: string = req.query['tokenBInputAmount'] as string; // 代币B的数量
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                expect(typeof _tokenAInputAmount !== 'undefined' || typeof _tokenBInputAmount !== 'undefined', 'tokenAInputAmount OR tokenBInputAmount 二传一').to.be.true;
                if (typeof _tokenAInputAmount !== 'undefined') expect(_tokenAInputAmount.trim(), 'tokenAInputAmount 无效').to.length.gt(0);
                if (typeof _tokenBInputAmount !== 'undefined') expect(_tokenBInputAmount.trim(), 'tokenBInputAmount 无效').to.length.gt(0);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
                    try {
                        let amountResult = null;
                        if (_tokenAInputAmount != null) {
                            amountResult = thePair.getOutputAmount(new TokenAmount(tokenA, _tokenAInputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBInputAmount != null) {
                            amountResult = thePair.getOutputAmount(new TokenAmount(tokenB, _tokenBInputAmount))[0].toSignificant(6);
                        }
                        return res.status(200).json({ code: 200, message: 'OK', data: amountResult });
                    } catch (err) {
                        next(err);
                    }
                })();
            });
        // 根据输出数量计算输入数量
        this.app.route(`/univ2-sdk/pair/getInputAmount`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _tokenAOutputAmount: string = req.query['tokenAOutputAmount'] as string; // 代币A的数量
                const _tokenBOutputAmount: string = req.query['tokenBOutputAmount'] as string; // 代币B的数量
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                expect(typeof _tokenAOutputAmount !== 'undefined' || typeof _tokenBOutputAmount !== 'undefined', 'tokenAOutputAmount OR tokenBOutputAmount 二传一').to.be.true;
                if (typeof _tokenAOutputAmount !== 'undefined') expect(_tokenAOutputAmount.trim(), 'tokenAOutputAmount 无效').to.length.gt(0);
                if (typeof _tokenBOutputAmount !== 'undefined') expect(_tokenBOutputAmount.trim(), 'tokenBOutputAmount 无效').to.length.gt(0);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
                    try {
                        let amountResult = null;
                        if (_tokenAOutputAmount != null) {
                            amountResult = thePair.getInputAmount(new TokenAmount(tokenA, _tokenAOutputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBOutputAmount != null) {
                            amountResult = thePair.getInputAmount(new TokenAmount(tokenB, _tokenBOutputAmount))[0].toSignificant(6);
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
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
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
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const thePair = await Fetcher.fetchPairData(tokenA, tokenB, getDefaultProvider(getNetwork(tokenA.chainId)));
                    const route = new Route([thePair], tokenB);
                    const trade = new Trade(route, new TokenAmount(tokenB, '1000000000000000'), TradeType.EXACT_INPUT);
                    let result = { code: 200, message: 'OK', data: trade };
                    res.status(200).json(result);
                })();
            });
        return this.app;
    }
}