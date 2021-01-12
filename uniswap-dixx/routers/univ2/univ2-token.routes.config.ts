import { CommonRoutesConfig } from '../../common/common.routes.config';
import express from 'express';
import { tokens as DefaultTokenList } from '@uniswap/default-token-list/build/uniswap-default.tokenlist.json';

export class UniV2TokenRoutes extends CommonRoutesConfig {
    constructor(app: express.Application) {
        super(app, 'UniV2TokenRoutes');
    }
    configureRoutes() {
        // 查询默认 token 列表
        this.app.route(`/univ2-sdk/default-token-list`)
            .get((req: express.Request, res: express.Response) => {
                const _chainId: number = req.query['chainId'] as any; // 链id see ChainId
                const _symbol: string = req.query['symbol'] as any; 
                const symbolNotFilter = _symbol === null || typeof _symbol === 'undefined' || _symbol === '';
                res.status(200).json({ code: 200, message: 'OK', data: DefaultTokenList.filter(t => t.chainId == _chainId && (symbolNotFilter || t.symbol.toLowerCase() === _symbol.toLowerCase()))});
            });
        return this.app;
    }
}