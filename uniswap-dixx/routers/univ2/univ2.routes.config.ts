import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { expect } from 'chai';
import { ChainId, Token, WETH, Fetcher, Route } from '@uniswap/sdk'

// import { ErrorHandler } from '../../common/error';

// https://uniswap.org/docs/v2/SDK/getting-started
export class UniV2sRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2sRoutes');
    }
    configureRoutes() {
        this.app.route(`/univ2/token`)
            .get((req: express.Request, res: express.Response, next: express.NextFunction) => {
                const _tokenAddr: string = req.query['tokenAddr'] as string; // 代币地址
                const _chainId: ChainId = ChainId['MAINNET']; // 链id
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
                let result = {
                    code: 200,
                    message: 'OK',
                    data: `This is UniswapV2 Pair Page, ChainId: ${ChainId.MAINNET}`,
                };
                res.status(200).json(result);
            });
        this.app.route(`/univ2/route`)
            .get((req: express.Request, res: express.Response) => {
                let result = {
                    code: 200,
                    message: 'OK',
                    data: `This is UniswapV2 Route Page, ChainId: ${ChainId.MAINNET}`,
                };
                res.status(200).json(result);
            });
        this.app.route(`/univ2/trade`)
            .get((req: express.Request, res: express.Response) => {
                let result = {
                    code: 200,
                    message: 'OK',
                    data: `This is UniswapV2 Trade Page, ChainId: ${ChainId.MAINNET}`,
                };
                res.status(200).json(result);
            });
        this.app.route(`/univ2/fractions`)
            .get((req: express.Request, res: express.Response) => {
                let result = {
                    code: 200,
                    message: 'OK',
                    data: `This is UniswapV2 Fractions Page, ChainId: ${ChainId.MAINNET}`,
                };
                res.status(200).json(result);
            });
        this.app.route(`/univ2/fetcher`)
            .get((req: express.Request, res: express.Response) => {
                let result = {
                    code: 200,
                    message: 'OK',
                    data: `This is UniswapV2 Fetcher Page, ChainId: ${ChainId.MAINNET}`,
                };
                res.status(200).json(result);
            });
        return this.app;
    }
}