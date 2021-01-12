import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import debug from 'debug';
import { expect } from 'chai';
import { simpleEncode } from 'ethereumjs-abi';
import Web3 from 'web3';
import { ChainId, Fetcher, Route, Pair, TokenAmount, TradeType, Trade, Price } from '@uniswap/sdk';

import { abi as IUniswapV2PairABI } from '@uniswap/v2-core/build/IUniswapV2Pair.json';

const loggerInfo: debug.IDebugger = debug('app-univ2-trade');

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
                    res.status(200).json({ code: 200, message: 'OK', data: token});
                })();
            });
        this.app.route(`/univ2-sdk/pair`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                const _method: string = req.query['method'] as string; // 想执行的方法
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const pair = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    let result = { code: 200, message: 'OK', data: null };
                    if (_method === 'reserve0') {
                        Object.assign(result, { data: { reserve0: pair.reserve0 } });
                    } else if (_method === 'reserve1') {
                        Object.assign(result, { data: { reserve1: pair.reserve1 } });
                    } else if (_method === 'token0Price') {
                        Object.assign(result, { data: { token0Price: pair.token0Price } });
                    } else if (_method === 'token1Price') {
                        Object.assign(result, { data: { token1Price: pair.token1Price } });
                    } else if (_method === 'token0') {
                        Object.assign(result, { data: { token0: pair.token0 } });
                    } else if (_method === 'token1') {
                        pair.getInputAmount(new TokenAmount(tokenA, '2000000000000000000'));
                        Object.assign(result, { data: { token1: pair.token1 } });
                    } else {
                        Object.assign(result, { data: pair });
                    }
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2-sdk/pair/getPairAddress`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const result = { code: 200, message: 'OK', data: Pair.getAddress(tokenA, tokenB) };
                    res.status(200).json(result);
                })();
            });
        // 查询当前交易对兑换价格
        this.app.route(`/univ2-sdk/pair/priceOf`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokenB: string = req.query['tokenB'] as string; // 代币地址B
                expect(_tokenA, 'tokenA 无效').to.have.lengthOf(42);
                expect(_tokenB, 'tokenB 无效').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokenA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokenB);
                    const pair = new Pair(new TokenAmount(tokenA, '8000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    try {
                        const priceA: Price  = pair.priceOf(tokenA);
                        const priceB: Price  = pair.priceOf(tokenB);
                        return res.status(200).json({ code: 200, message: 'OK', data: [ priceA.toSignificant(6), priceB.toSignificant(6) ] });
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
                    const pair = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    try {
                        let amountResult = null;
                        if (_tokenAInputAmount != null) {
                            amountResult = pair.getOutputAmount(new TokenAmount(tokenA, _tokenAInputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBInputAmount != null) {
                            amountResult = pair.getOutputAmount(new TokenAmount(tokenB, _tokenBInputAmount))[0].toSignificant(6);
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
                    const pair = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    try {
                        let amountResult = null;
                        if (_tokenAOutputAmount != null) {
                            amountResult = pair.getInputAmount(new TokenAmount(tokenA, _tokenAOutputAmount))[0].toSignificant(6);
                        }
                        if (_tokenBOutputAmount != null) {
                            amountResult = pair.getInputAmount(new TokenAmount(tokenB, _tokenBOutputAmount))[0].toSignificant(6);
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
                    const HOT_NOT = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    const route = new Route([HOT_NOT], tokenB);
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
                    const pair = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    const route = new Route([pair], tokenB);
                    const trade = new Trade(route, new TokenAmount(tokenB, '1000000000000000'), TradeType.EXACT_INPUT);
                    let result = { code: 200, message: 'OK', data: trade };
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2-sdk/abi`)
            .post((req: express.Request, res: express.Response) => {
                const _addr: string = req.query['addr'] as string;
                const _fname: string = req.query['fname'] as string;
                //const _swapExactETHForTokensABI = 'function swapExactETHForTokens(uint amountOutMin, address[] calldata path, address to, uint deadline) external payable returns (uint[] memory amounts);';
                const _swapExactETHForTokensABI = 'balanceOf(address):(uint256)';
                expect(_addr, 'addr 无效').to.have.lengthOf(42);
                expect(_fname, 'fname 无效').to.length.gte(5);
                let encodedAbiHex = null;
                if (_fname === 'swapExactETHForTokens') {
                    encodedAbiHex = simpleEncode(_swapExactETHForTokensABI, _addr).toString('hex');
                }
                loggerInfo(`encoded ABI: ${encodedAbiHex}`);
                res.status(200).json(encodedAbiHex);
            });
        return this.app;
    }
}