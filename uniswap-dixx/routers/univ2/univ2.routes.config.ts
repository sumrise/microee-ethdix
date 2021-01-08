import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { expect } from 'chai';
import { ChainId, Fetcher, Route, Pair, TokenAmount, TradeType, Trade } from '@uniswap/sdk'

// https://www.youtube.com/watch?v=0Im5iaYoz1Y
// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2sRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2sRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2/token`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                expect(_tokenAddr, 'tokenAddr invalid').to.have.lengthOf(42);
                (async () => {
                    const token = await Fetcher.fetchTokenData(_chainId, _tokenAddr); // 根据代币地址获取erc20合约信息
                    let result = {
                        code: 200,
                        message: 'OK',
                        data: token,
                    };
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2/pair`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokanA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokanB: string = req.query['tokenB'] as string; // 代币地址B
                expect(_tokanA, 'tokanA invalid').to.have.lengthOf(42);
                expect(_tokanB, 'tokanB invalid').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokanA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokanB);
                    const pair = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    let result = {
                        code: 200,
                        message: 'OK',
                        data: pair,
                    };
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2/route`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokanA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokanB: string = req.query['tokenB'] as string; // 代币地址B
                expect(_tokanA, 'tokanA invalid').to.have.lengthOf(42);
                expect(_tokanB, 'tokanB invalid').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokanA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokanB);
                    const HOT_NOT = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    const route = new Route([HOT_NOT], tokenB);
                    let result = {
                        code: 200,
                        message: 'OK',
                        data: route,
                    };
                    res.status(200).json(result);
                })();
            });
        this.app.route(`/univ2/trade`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
                const _tokanA: string = req.query['tokenA'] as string; // 代币地址A
                const _tokanB: string = req.query['tokenB'] as string; // 代币地址B
                expect(_tokanA, 'tokanA invalid').to.have.lengthOf(42);
                expect(_tokanB, 'tokanB invalid').to.have.lengthOf(42);
                (async () => {
                    const tokenA = await Fetcher.fetchTokenData(_chainId, _tokanA);
                    const tokenB = await Fetcher.fetchTokenData(_chainId, _tokanB);
                    const HOT_NOT = new Pair(new TokenAmount(tokenA, '2000000000000000000'), new TokenAmount(tokenB, '1000000000000000000'));
                    const NOT_TO_HOT = new Route([HOT_NOT], tokenB);
                    const trade = new Trade(NOT_TO_HOT, new TokenAmount(tokenB, '1000000000000000'), TradeType.EXACT_INPUT);
                    let result = {
                        code: 200,
                        message: 'OK',
                        data: trade,
                    };
                    res.status(200).json(result);
                })();
            });
        return this.app;
    }
}